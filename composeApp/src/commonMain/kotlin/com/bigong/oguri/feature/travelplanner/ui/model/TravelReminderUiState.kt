package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelReminderSettings
import com.bigong.oguri.domain.model.TravelReminderTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal data class TravelReminderUiState(
    val settings: TravelReminderSettings? = null,
    val busy: Boolean = false,
    val failure: TravelPlanFailure? = null,
    val permissionDenied: Boolean = false,
    val openedPlanId: String? = null,
    val openedReminderId: String? = null,
)

internal fun parseTravelReminderTime(
    date: String,
    time: String,
    zone: String,
    settings: TravelReminderSettings,
    now: Instant,
): TravelReminderTime? =
    runCatching {
        val timeZone = TimeZone.of(zone.trim())
        val local = LocalDateTime(LocalDate.parse(date.trim()), LocalTime.parse(time.trim()))
        val instant = local.toInstant(timeZone)
        require(instant.toLocalDateTime(timeZone) == local)
        require(instant > now && instant < settings.dates.start.atStartOfDayIn(settings.tripTimeZone))
        TravelReminderTime(local, timeZone)
    }.getOrNull()
