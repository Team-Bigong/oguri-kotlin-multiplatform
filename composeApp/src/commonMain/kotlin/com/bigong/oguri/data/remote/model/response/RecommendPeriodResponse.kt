package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class RecommendPeriodResponse(
    val rank: Int,
    val isSaved: Boolean,
    val startDate: String,
    val endDate: String,
    val holiday: List<String>,
    val dayOffCount: Int,
    val totalTripCount: Int,
    val places: List<PlaceResponse>,
    val advertisements: List<AdvertisementResponse>,
)
