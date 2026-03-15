package com.bigong.oguri.dto

import java.time.LocalDate

data class SaveRecommendationRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dayOffCount: Int,
    val totalTripCount: Int
)
