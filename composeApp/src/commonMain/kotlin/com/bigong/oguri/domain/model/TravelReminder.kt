package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class TravelReminderStatus { PENDING, HANDOFF_STARTED, SENT, FAILED, EXPIRED }

data class TravelReminder(
    val id: String,
    val scheduledAt: Instant,
    val timeZone: TimeZone,
    val status: TravelReminderStatus,
)

data class TravelReminderSettings(
    val dates: TravelDates,
    val tripTimeZone: TimeZone,
    val canSchedule: Boolean,
    val reminder: TravelReminder?,
)

data class TravelReminderTime(val localDateTime: LocalDateTime, val timeZone: TimeZone)
