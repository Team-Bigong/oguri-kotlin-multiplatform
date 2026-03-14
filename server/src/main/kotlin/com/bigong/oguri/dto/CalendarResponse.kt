package com.bigong.oguri.dto

import java.time.LocalDate

data class CalendarResponse(
    val dayOffCount: Int,
    val bestPeriods: List<BestPeriodResponse>,
    val holidays: List<HolidayResponse>
)

data class BestPeriodResponse(
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class HolidayResponse(
    val date: LocalDate,
    val label: String
)
