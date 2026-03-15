package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class CalendarPeriodDetail(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val holiday: List<String>,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val places: List<Place>,
)
