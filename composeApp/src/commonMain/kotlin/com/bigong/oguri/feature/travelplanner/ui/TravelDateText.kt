package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.runtime.Composable
import com.bigong.oguri.domain.model.TravelDates
import kotlinx.datetime.LocalDate
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_calendar_date
import oguri.composeapp.generated.resources.travel_dates_value
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun travelDateLabel(date: LocalDate): String =
    stringResource(Res.string.travel_calendar_date, date.year, date.month.ordinal + 1, date.day)

@Composable
internal fun travelDateRange(dates: TravelDates): String {
    val start = travelDateLabel(dates.start)
    return if (dates.dayCount == 1) {
        start
    } else {
        stringResource(Res.string.travel_dates_value, start, travelDateLabel(dates.endInclusive))
    }
}
