package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class RecommendPeriod(
    val rank: Int,
    val isSaved: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val holiday: List<String>,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val places: List<Place>,
    val advertisements: List<Advertisement>,
)
