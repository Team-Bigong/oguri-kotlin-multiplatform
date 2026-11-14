package com.bigong.oguri.data.remote

import com.bigong.oguri.core.analytics.travelUsageSession
import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.core.network.BASE_URL
import com.bigong.oguri.core.network.RequestAccessToken
import com.bigong.oguri.data.remote.model.request.CreateTravelPlanRequest
import com.bigong.oguri.data.remote.model.request.RecordTravelActivityRequest
import com.bigong.oguri.data.remote.model.request.RecordTravelPlanViewRequest
import com.bigong.oguri.data.remote.model.request.TravelChangesSeenRequest
import com.bigong.oguri.data.remote.model.request.TravelCityTimeZoneRequest
import com.bigong.oguri.data.remote.model.request.TravelDetailsRequest
import com.bigong.oguri.data.remote.model.request.TravelInviteRequest
import com.bigong.oguri.data.remote.model.request.TravelOwnershipRequest
import com.bigong.oguri.data.remote.model.request.TravelPlaceBatchRequest
import com.bigong.oguri.data.remote.model.request.TravelPlaceSearchRequest
import com.bigong.oguri.data.remote.model.request.TravelPlanCommandRequest
import com.bigong.oguri.data.remote.model.response.IssuedTravelInviteResponse
import com.bigong.oguri.data.remote.model.response.JoinedTravelPlanResponse
import com.bigong.oguri.data.remote.model.response.TravelCitySuggestionResponse
import com.bigong.oguri.data.remote.model.response.TravelCityTimeZoneResponse
import com.bigong.oguri.data.remote.model.response.TravelDayPlacesResponse
import com.bigong.oguri.data.remote.model.response.TravelInvitePreviewResponse
import com.bigong.oguri.data.remote.model.response.TravelMembersResponse
import com.bigong.oguri.data.remote.model.response.TravelPlaceResponse
import com.bigong.oguri.data.remote.model.response.TravelPlanResponse
import com.bigong.oguri.data.remote.model.response.TravelPlanResumeResponse
import com.bigong.oguri.data.remote.model.response.TravelPlanSummaryResponse
import com.bigong.oguri.data.remote.model.response.TravelRecentChangesResponse
import com.bigong.oguri.domain.model.TravelUsageActivity
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Inject
internal class KtorTravelPlanRemoteDataSource(
    private val httpClient: HttpClient,
    private val authRequestExecutor: AuthRequestExecutor,
) {
    private val sessionVersion: Long = AuthTokenStore.currentSession().version

    private val activityMutex = Mutex()
    private var activitySession: String? = null
    private val recordedActivities = mutableSetOf<Pair<String, TravelUsageActivity>>()

    private suspend fun <T> usageRequest(block: suspend (String, String) -> T): T =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            val session = travelUsageSession.current(sessionVersion) ?: throw AuthSessionChangedException()
            block(token, session)
        }

    suspend fun recordActivity(
        planId: String,
        kind: TravelUsageActivity,
    ) {
        usageRequest { token, session ->
            activityMutex.withLock {
                if (activitySession != session) {
                    activitySession = session
                    recordedActivities.clear()
                }
                val key = planId to kind
                if (key !in recordedActivities) {
                    httpClient.post("$BASE_URL$TRIPS_PATH/$planId/activity") {
                        attributes.put(RequestAccessToken, token)
                        header("X-Travel-Session", session)
                        contentType(ContentType.Application.Json)
                        setBody(RecordTravelActivityRequest(kind.name))
                    }
                    recordedActivities.add(key)
                }
            }
        }
    }

    suspend fun list(): List<TravelPlanSummaryResponse> =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.get("$BASE_URL$TRIPS_PATH") { attributes.put(RequestAccessToken, token) }.body()
        }

    suspend fun resume(): TravelPlanResumeResponse =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.get("$BASE_URL$TRIPS_PATH/resume") { attributes.put(RequestAccessToken, token) }.body()
        }

    suspend fun recordView(
        planId: String,
        dayNumber: Int,
    ) {
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.put("$BASE_URL$TRIPS_PATH/$planId/view") {
                attributes.put(RequestAccessToken, token)
                contentType(ContentType.Application.Json)
                setBody(RecordTravelPlanViewRequest(dayNumber))
            }
        }
    }

    suspend fun create(request: CreateTravelPlanRequest): TravelPlanSummaryResponse =
        usageRequest { token, usageSession ->
            httpClient
                .post("$BASE_URL$TRIPS_PATH") {
                    attributes.put(RequestAccessToken, token)
                    header("X-Travel-Session", usageSession)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
        }

    suspend fun get(planId: String): TravelPlanResponse =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.get("$BASE_URL$TRIPS_PATH/$planId") { attributes.put(RequestAccessToken, token) }.body()
        }

    suspend fun getIfChanged(
        planId: String,
        knownRevision: Long,
    ): TravelPlanResponse? =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            val response =
                httpClient.get("$BASE_URL$TRIPS_PATH/$planId") {
                    attributes.put(RequestAccessToken, token)
                    parameter("knownRevision", knownRevision)
                }
            if (response.status == HttpStatusCode.NoContent) null else response.body()
        }

    suspend fun changeVisit(
        planId: String,
        request: TravelPlanCommandRequest,
    ): TravelPlanResponse =
        usageRequest { token, usageSession ->
            httpClient
                .post("$BASE_URL$TRIPS_PATH/$planId/commands") {
                    attributes.put(RequestAccessToken, token)
                    header("X-Travel-Session", usageSession)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
        }

    suspend fun searchPlaces(
        planId: String,
        query: String,
        language: String,
    ): List<TravelPlaceResponse> =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient
                .post("$BASE_URL$TRIPS_PATH/$planId/places/search") {
                    attributes.put(RequestAccessToken, token)
                    contentType(ContentType.Application.Json)
                    setBody(TravelPlaceSearchRequest(query, language))
                }.body()
        }

    suspend fun planPlaces(
        planId: String,
        language: String,
        placeIds: List<String>,
    ): TravelDayPlacesResponse =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient
                .post("$BASE_URL$TRIPS_PATH/$planId/places/details") {
                    attributes.put(RequestAccessToken, token)
                    contentType(ContentType.Application.Json)
                    setBody(TravelPlaceBatchRequest(placeIds, language))
                }.body()
        }

    suspend fun dayPlaces(
        planId: String,
        dayNumber: Int,
        language: String,
    ): TravelDayPlacesResponse =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient
                .get("$BASE_URL$TRIPS_PATH/$planId/days/$dayNumber/places") {
                    attributes.put(RequestAccessToken, token)
                    parameter("language", language)
                }.body()
        }

    suspend fun cities(
        query: String,
        language: String,
    ): List<TravelCitySuggestionResponse> =
        authRequestExecutor.execute {
            httpClient
                .post("$BASE_URL$CITIES_PATH/search") {
                    contentType(ContentType.Application.Json)
                    setBody(TravelPlaceSearchRequest(query, language))
                }.body()
        }

    suspend fun cityTimeZone(placeId: String): TravelCityTimeZoneResponse =
        authRequestExecutor.execute {
            httpClient
                .post("$BASE_URL$CITIES_PATH/time-zone") {
                    contentType(ContentType.Application.Json)
                    setBody(TravelCityTimeZoneRequest(placeId))
                }.body()
        }

    suspend fun members(planId: String): TravelMembersResponse =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.get("$BASE_URL$TRIPS_PATH/$planId/members") { attributes.put(RequestAccessToken, token) }.body()
        }

    suspend fun issueInvite(planId: String): IssuedTravelInviteResponse =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.post("$BASE_URL$TRIPS_PATH/$planId/invites") { attributes.put(RequestAccessToken, token) }.body()
        }

    suspend fun revokeInvite(planId: String): Unit = deleteAt("$TRIPS_PATH/$planId/invites")

    suspend fun removeMember(
        planId: String,
        memberId: String,
    ): Unit = deleteAt("$TRIPS_PATH/$planId/members/${memberId.encodeURLPathPart()}")

    suspend fun leave(planId: String): Unit = deleteAt("$TRIPS_PATH/$planId/membership")

    suspend fun deletePlan(planId: String): Unit = deleteAt("$TRIPS_PATH/$planId")

    private suspend fun deleteAt(path: String) {
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.delete("$BASE_URL$path") { attributes.put(RequestAccessToken, token) }
        }
    }

    suspend fun updateDetails(
        planId: String,
        request: TravelDetailsRequest,
    ) {
        usageRequest { token, usageSession ->
            httpClient.put("$BASE_URL$TRIPS_PATH/$planId/details") {
                attributes.put(RequestAccessToken, token)
                header("X-Travel-Session", usageSession)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun offerOwnership(
        planId: String,
        targetId: String,
    ) {
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.post("$BASE_URL$TRIPS_PATH/$planId/ownership") {
                attributes.put(RequestAccessToken, token)
                contentType(ContentType.Application.Json)
                setBody(TravelOwnershipRequest(targetId))
            }
        }
    }

    suspend fun acceptOwnership(planId: String) {
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.post("$BASE_URL$TRIPS_PATH/$planId/ownership/accept") { attributes.put(RequestAccessToken, token) }
        }
    }

    suspend fun recentChanges(planId: String): TravelRecentChangesResponse =
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.get("$BASE_URL$TRIPS_PATH/$planId/changes") { attributes.put(RequestAccessToken, token) }.body()
        }

    suspend fun markChangesSeen(
        planId: String,
        revision: Long,
    ) {
        authRequestExecutor.executeForSession(sessionVersion) { token ->
            httpClient.put("$BASE_URL$TRIPS_PATH/$planId/changes/seen") {
                attributes.put(RequestAccessToken, token)
                contentType(ContentType.Application.Json)
                setBody(TravelChangesSeenRequest(revision))
            }
        }
    }

    suspend fun previewInvite(token: String): TravelInvitePreviewResponse =
        httpClient
            .post("$BASE_URL$INVITES_PATH/preview") {
                attributes.put(com.bigong.oguri.core.network.OmitAuthentication, true)
                contentType(ContentType.Application.Json)
                setBody(TravelInviteRequest(token))
            }.body()

    suspend fun joinInvite(inviteToken: String): JoinedTravelPlanResponse =
        usageRequest { token, usageSession ->
            httpClient
                .post("$BASE_URL$INVITES_PATH/join") {
                    attributes.put(RequestAccessToken, token)
                    header("X-Travel-Session", usageSession)
                    contentType(ContentType.Application.Json)
                    setBody(TravelInviteRequest(inviteToken))
                }.body()
        }

    private companion object {
        const val CITIES_PATH: String = "/api/v1/travel-places/cities"
        const val INVITES_PATH: String = "/api/v1/trip-invites"
        const val TRIPS_PATH: String = "/api/v1/trips"
    }
}
