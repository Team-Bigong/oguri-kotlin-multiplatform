package com.bigong.oguri.dto

import java.time.LocalDate

data class RecommendPeriodResponse(
    val rank: Int,
    val isSaved: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val holiday: List<String>,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val places: List<PlaceResponse>,
    val advertisements: List<AdvertisementResponse>
)

data class PlaceResponse(
    val id: Long,
    val country: String,
    val city: String,
    val summary: String,
    val thumbnailUrl: String
)

data class AdvertisementResponse(
    val platform: String,
    val url: String
)
