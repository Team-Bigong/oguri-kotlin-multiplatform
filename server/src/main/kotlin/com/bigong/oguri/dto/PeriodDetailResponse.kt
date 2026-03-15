package com.bigong.oguri.dto

import java.time.LocalDate

data class PeriodDetailResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val holiday: List<String>,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val places: List<PlaceResponse>
)
