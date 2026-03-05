package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendPeriodResponse(
    @SerialName("rank")
    val rank: Int,
    @SerialName("saved")
    val saved: Boolean,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("holiday")
    val holiday: List<String>,
    @SerialName("dayOffCount")
    val dayOffCount: Int,
    @SerialName("totalTripCount")
    val totalTripCount: Int,
    @SerialName("places")
    val places: List<PlaceResponse>,
    @SerialName("advertisements")
    val advertisements: List<AdvertisementResponse>,
)
