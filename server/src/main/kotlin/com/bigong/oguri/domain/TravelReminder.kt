package com.bigong.oguri.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

data class TravelReminderTime private constructor(
    val instant: Instant,
    val timeZone: String,
) {
    companion object {
        fun beforeDeparture(
            localDateTime: LocalDateTime,
            timeZone: String,
            tripStartDate: LocalDate,
            tripTimeZone: String,
            now: Instant,
        ): TravelReminderTime {
            require(timeZone in ZoneId.getAvailableZoneIds())
            val zone = ZoneId.of(timeZone)
            val offset = zone.rules.getValidOffsets(localDateTime).singleOrNull()
                ?: throw IllegalArgumentException("Reminder time does not identify one instant")
            val instant = localDateTime.toInstant(offset)
            val departureDay = tripStartDate.atStartOfDay(ZoneId.of(tripTimeZone)).toInstant()
            require(instant.isAfter(now) && instant.isBefore(departureDay))
            return TravelReminderTime(instant, zone.id)
        }
    }

    fun utc(): java.time.OffsetDateTime = instant.atOffset(ZoneOffset.UTC)
}
