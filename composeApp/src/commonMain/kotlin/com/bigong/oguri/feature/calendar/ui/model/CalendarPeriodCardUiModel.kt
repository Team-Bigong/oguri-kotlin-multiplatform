package com.bigong.oguri.feature.calendar.ui.model

import com.bigong.oguri.domain.model.CalendarHoliday
import kotlinx.datetime.LocalDate

data class CalendarPeriodCardUiModel(
    val id: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dDay: Int,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val holidayNames: List<String>,
    val holidays: List<CalendarHoliday>,
)
