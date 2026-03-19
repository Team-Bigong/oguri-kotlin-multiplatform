package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class CalendarPeriod(
    val id: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalTripCount: Int,
    val holidayCount: Int,
    val dayOffCount: Int,
    val holidayNames: List<String>,
    val holidayDateDetails: List<CalendarHoliday>,
    val isSaved: Boolean,
)
