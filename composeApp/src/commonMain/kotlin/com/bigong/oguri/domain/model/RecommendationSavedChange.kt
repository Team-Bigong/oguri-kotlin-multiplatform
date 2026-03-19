package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class RecommendationSavedChange(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val isSaved: Boolean,
)
