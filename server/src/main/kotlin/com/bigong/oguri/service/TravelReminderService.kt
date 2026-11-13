package com.bigong.oguri.service

import com.bigong.oguri.domain.TravelPlanConflict
import com.bigong.oguri.domain.TravelReminderTime
import com.bigong.oguri.repository.TravelReminderRepository
import com.bigong.oguri.repository.TravelReminderRow
import com.bigong.oguri.repository.TravelReminderStatus
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.HexFormat
import java.util.UUID

@Service
class TravelReminderService(
    private val plans: TravelPlanService,
    private val reminders: TravelReminderRepository,
    private val delivery: TravelReminderDelivery,
) {
    @Transactional(readOnly = true)
    fun settings(memberId: String, planId: UUID): TravelReminderSettings {
        plans.requireMember(memberId)
        val plan = plans.accessiblePlan(memberId, planId)
        val current = reminders.current(planId, memberId)?.toResponse()
        val canSchedule = Instant.now().isBefore(plan.startDate.atStartOfDay(ZoneId.of(plan.timeZone)).toInstant())
        return TravelReminderSettings(plan.startDate.toString(), plan.endDate.toString(), plan.timeZone, delivery.configured && canSchedule, current)
    }

    @Transactional
    fun schedule(memberId: String, planId: UUID, request: ScheduleTravelReminder): TravelReminder {
        plans.requireMember(memberId)
        val plan = plans.accessiblePlan(memberId, planId, write = true)
        if (!delivery.configured) throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)
        val deviceId = request.deviceId
        if (reminders.lockActiveDevice(deviceId, memberId) == null) throw TravelPlanConflict("REMINDER_DEVICE_REVOKED")

        val previous = reminders.find(request.requestId)
        if (previous != null) {
            if (previous.memberId != memberId || previous.planId != planId || previous.deviceId != deviceId ||
                previous.timeZone != request.timeZone ||
                previous.scheduledAt.atZoneSameInstant(ZoneId.of(previous.timeZone)).toLocalDateTime() != request.localDateTime
            ) {
                throw TravelPlanConflict("REQUEST_ID_REUSED")
            }
            if (previous.status == TravelReminderStatus.CANCELLED) throw TravelPlanConflict("REMINDER_CANCELLED")
            return previous.toResponse()
        }

        if (request.expectedStartDate != plan.startDate || request.expectedEndDate != plan.endDate ||
            request.expectedTripTimeZone != plan.timeZone
        ) {
            throw TravelPlanConflict("TRIP_STRUCTURE_CHANGED")
        }
        val time = TravelReminderTime.beforeDeparture(request.localDateTime, request.timeZone, plan.startDate, plan.timeZone, Instant.now())
        reminders.cancelForMember(planId, memberId)
        reminders.insert(request.requestId, planId, memberId, deviceId, time.utc(), time.timeZone)
        return TravelReminder(request.requestId, time.utc(), time.timeZone, TravelReminderStatus.PENDING)
    }

    @Transactional
    fun cancel(memberId: String, planId: UUID, reminderId: UUID) {
        plans.requireMember(memberId)
        plans.accessiblePlan(memberId, planId, write = true)
        reminders.cancelVersion(planId, memberId, reminderId)
    }

    @Transactional
    fun registerDevice(memberId: String, deviceId: UUID, device: RegisterTravelReminderDevice) {
        plans.requireMember(memberId)
        if (!delivery.configured) throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)
        require(device.pushToken.length in 16..4096 && device.pushToken.all { it in '!'..'~' })
        val revocationHash = hash(device.revokeSecret)
        reminders.lockDeviceIdentity(deviceId)
        if (!reminders.bindDevice(deviceId, memberId, revocationHash, device.pushToken)) {
            throw TravelPlanConflict("REMINDER_DEVICE_REVOKED")
        }
    }

    @Transactional
    fun revokeDevice(deviceId: UUID, secret: String) {
        val revocationHash = hash(secret)
        reminders.lockDeviceIdentity(deviceId)
        reminders.revokeDevice(deviceId, revocationHash)
    }

    @Transactional(readOnly = true)
    fun open(memberId: String, reminderId: UUID): UUID {
        plans.requireMember(memberId)
        val reminder = reminders.find(reminderId)
            ?.takeIf { it.memberId == memberId && it.status in setOf(TravelReminderStatus.HANDOFF_STARTED, TravelReminderStatus.SENT) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        plans.accessiblePlan(memberId, reminder.planId)
        if (reminders.current(reminder.planId, memberId)?.id != reminderId) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return reminder.planId
    }

    @Transactional
    fun beginHandoff(candidate: TravelReminderRow): String? {
        try {
            plans.accessiblePlan(candidate.memberId, candidate.planId, write = true)
        } catch (failure: ResponseStatusException) {
            if (failure.statusCode == HttpStatus.NOT_FOUND) return null
            throw failure
        }
        val token = reminders.lockActiveDevice(candidate.deviceId, candidate.memberId) ?: return null
        return token.takeIf { reminders.beginHandoff(candidate.id) }
    }

    private fun hash(secret: String): String {
        require(SECRET.matches(secret))
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(secret.toByteArray()))
    }

    private fun TravelReminderRow.toResponse(): TravelReminder = TravelReminder(id, scheduledAt, timeZone, status)

    private companion object {
        val SECRET: Regex = Regex("[a-z0-9]{64}")
    }
}

@JvmRecord
data class TravelReminderSettings(
    val tripStartDate: String,
    val tripEndDate: String,
    val tripTimeZone: String,
    val canSchedule: Boolean,
    val reminder: TravelReminder?,
)

@JvmRecord
data class TravelReminder(
    val id: UUID,
    val scheduledAt: OffsetDateTime,
    val timeZone: String,
    val status: TravelReminderStatus,
)

@JvmRecord
data class ScheduleTravelReminder(
    val requestId: UUID,
    val localDateTime: LocalDateTime,
    val timeZone: String,
    val expectedStartDate: java.time.LocalDate,
    val expectedEndDate: java.time.LocalDate,
    val expectedTripTimeZone: String,
    val deviceId: UUID,
)

@JvmRecord
data class RegisterTravelReminderDevice(val revokeSecret: String, val pushToken: String)
