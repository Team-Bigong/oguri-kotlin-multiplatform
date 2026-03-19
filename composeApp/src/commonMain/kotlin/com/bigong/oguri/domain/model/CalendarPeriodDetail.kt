package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class CalendarPeriodDetail(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val holiday: List<String>,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    val places: List<Place>,
)
