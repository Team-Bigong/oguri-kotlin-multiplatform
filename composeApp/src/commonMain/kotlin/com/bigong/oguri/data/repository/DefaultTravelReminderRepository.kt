package com.bigong.oguri.data.repository

import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.core.network.BASE_URL
import com.bigong.oguri.core.network.OmitAuthentication
import com.bigong.oguri.core.network.RequestAccessToken
import com.bigong.oguri.core.notification.clearTravelReminderPushToken
import com.bigong.oguri.core.notification.readTravelReminderDeviceState
import com.bigong.oguri.core.notification.travelReminderPushToken
import com.bigong.oguri.core.notification.writeTravelReminderDeviceState
import com.bigong.oguri.data.remote.AuthRequestExecutor
import com.bigong.oguri.data.remote.AuthSessionChangedException
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlanException
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelReminder
import com.bigong.oguri.domain.model.TravelReminderSettings
import com.bigong.oguri.domain.model.TravelReminderStatus
import com.bigong.oguri.domain.model.TravelReminderTime
import com.bigong.oguri.domain.repository.TravelReminderRepository
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Inject
class DefaultTravelReminderRepository(
    private val client: HttpClient,
    private val auth: AuthRequestExecutor,
) : TravelReminderRepository {
    private val sessionVersion: Long = AuthTokenStore.currentSession().version

    override suspend fun settings(planId: String): TravelReminderSettings =
        request { token ->
            client.get("$BASE_URL/api/v1/trips/$planId/reminder") { authorize(token) }.body<ReminderSettingsResponse>().toDomain()
        }

    override suspend fun schedule(
        planId: String,
        settings: TravelReminderSettings,
        time: TravelReminderTime,
    ): TravelReminder {
        val requestId = newRequestId()
        return request { token ->
            val account = AuthTokenStore.currentSession().accountKey ?: throw AuthSessionChangedException()
            val device =
                deviceMutex.withLock {
                    checkSession()
                    syncDeviceAccount(account)
                    val state = readState()
                    state.device ?: newDevice(account).also { writeState(state.copy(device = it)) }
                }
            val body =
                ScheduleReminderRequest(
                    requestId = requestId,
                    localDateTime = time.localDateTime.toString(),
                    timeZone = time.timeZone.id,
                    expectedStartDate = settings.dates.start.toString(),
                    expectedEndDate = settings.dates.endInclusive.toString(),
                    expectedTripTimeZone = settings.tripTimeZone.id,
                    deviceId = device.id,
                )
            val pushToken = travelReminderPushToken() ?: throw TravelPlanException(TravelPlanFailure.CONNECTION)
            checkSession()
            register(token, device, pushToken)
            checkSession()
            client
                .put("$BASE_URL/api/v1/trips/$planId/reminder") {
                    authorize(token)
                    jsonBody(body)
                }.body<ReminderResponse>()
                .toDomain()
        }
    }

    override suspend fun cancel(
        planId: String,
        reminderId: String,
    ) {
        request { token -> client.delete("$BASE_URL/api/v1/trips/$planId/reminder/$reminderId") { authorize(token) } }
    }

    override suspend fun open(reminderId: String): String =
        request { token ->
            client.get("$BASE_URL/api/v1/travel-reminders/$reminderId") { authorize(token) }.body<OpenReminderResponse>().planId
        }

    override suspend fun syncDevice() {
        checkSession()
        val session = AuthTokenStore.currentSession()
        val account = session.accountKey.takeIf { session.tokens != null }
        val device =
            deviceMutex.withLock {
                checkSession()
                syncDeviceAccount(account)
                readState().device
            }
        if (device != null && account != null) {
            val pushToken = travelReminderPushToken() ?: return
            request { token -> register(token, device, pushToken) }
        }
    }

    private suspend fun syncDeviceAccount(account: String?) {
        val stored = readState()
        val active = stored.device
        val state =
            if (active != null && active.account != account) {
                stored.copy(device = null, revocations = stored.revocations + active).also {
                    writeState(it)
                    clearTravelReminderPushToken()
                }
            } else {
                stored
            }
        val remaining =
            state.revocations.filterNot { device ->
                try {
                    client.post("$BASE_URL/api/v1/travel-reminder-devices/${device.id}/revoke") {
                        attributes.put(OmitAuthentication, true)
                        jsonBody(RevokeDeviceRequest(device.secret))
                    }
                    true
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    false
                }
            }
        if (remaining != state.revocations) writeState(state.copy(revocations = remaining))
    }

    private suspend fun register(
        token: String,
        device: ReminderDevice,
        pushToken: String,
    ) {
        client.put("$BASE_URL/api/v1/travel-reminder-devices/${device.id}") {
            authorize(token)
            jsonBody(RegisterDeviceRequest(device.secret, pushToken))
        }
    }

    private fun checkSession() {
        if (AuthTokenStore.currentSession().version != sessionVersion) throw AuthSessionChangedException()
    }

    private suspend fun <Result> request(block: suspend (String) -> Result): Result =
        try {
            auth.executeForSession(sessionVersion, block)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: TravelPlanException) {
            throw failure
        } catch (failure: AuthSessionChangedException) {
            throw TravelPlanException(TravelPlanFailure.SIGN_IN_REQUIRED, failure)
        } catch (failure: ClientRequestException) {
            val reason =
                when (failure.response.status) {
                    HttpStatusCode.Unauthorized -> TravelPlanFailure.SIGN_IN_REQUIRED
                    HttpStatusCode.NotFound, HttpStatusCode.Forbidden -> TravelPlanFailure.UNAVAILABLE
                    HttpStatusCode.BadRequest -> TravelPlanFailure.INVALID
                    HttpStatusCode.Conflict -> TravelPlanFailure.CONFLICT
                    else -> TravelPlanFailure.CONNECTION
                }
            throw TravelPlanException(reason, failure)
        } catch (failure: Exception) {
            throw TravelPlanException(TravelPlanFailure.CONNECTION, failure)
        }

    private fun HttpRequestBuilder.authorize(token: String) = attributes.put(RequestAccessToken, token)

    private inline fun <reified Body> HttpRequestBuilder.jsonBody(value: Body) {
        contentType(ContentType.Application.Json)
        setBody(value)
    }

    private fun readState(): ReminderDeviceState =
        readTravelReminderDeviceState()?.let { Json.decodeFromString<ReminderDeviceState>(it) } ?: ReminderDeviceState()

    private fun writeState(state: ReminderDeviceState) = writeTravelReminderDeviceState(Json.encodeToString(state))

    private fun newDevice(account: String): ReminderDevice =
        ReminderDevice(newRequestId(), account, (newRequestId() + newRequestId()).replace("-", ""))

    @OptIn(ExperimentalUuidApi::class)
    private fun newRequestId(): String = Uuid.random().toString()

    private companion object {
        val deviceMutex: Mutex = Mutex()
    }
}

@Serializable
private data class ReminderDeviceState(
    val device: ReminderDevice? = null,
    val revocations: List<ReminderDevice> = emptyList(),
)

@Serializable
private data class ReminderDevice(
    val id: String,
    val account: String,
    val secret: String,
)

@Serializable
private data class RegisterDeviceRequest(
    val revokeSecret: String,
    val pushToken: String,
)

@Serializable
private data class RevokeDeviceRequest(
    val secret: String,
)

@Serializable
private data class ScheduleReminderRequest(
    val requestId: String,
    val localDateTime: String,
    val timeZone: String,
    val expectedStartDate: String,
    val expectedEndDate: String,
    val expectedTripTimeZone: String,
    val deviceId: String,
)

@Serializable
private data class OpenReminderResponse(
    val planId: String,
)

@Serializable
private data class ReminderSettingsResponse(
    val tripStartDate: LocalDate,
    val tripEndDate: LocalDate,
    val tripTimeZone: String,
    val canSchedule: Boolean,
    val reminder: ReminderResponse?,
) {
    fun toDomain(): TravelReminderSettings =
        TravelReminderSettings(TravelDates(tripStartDate, tripEndDate), TimeZone.of(tripTimeZone), canSchedule, reminder?.toDomain())
}

@Serializable
private data class ReminderResponse(
    val id: String,
    val scheduledAt: String,
    val timeZone: String,
    val status: TravelReminderStatus,
) {
    fun toDomain(): TravelReminder = TravelReminder(id, Instant.parse(scheduledAt), TimeZone.of(timeZone), status)
}
