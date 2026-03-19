package com.bigong.oguri.data.remote.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManageSavedRecommendationRequest(
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("dayOffCount")
    val dayOffCount: Int,
    @SerialName("totalTripCount")
    val totalTripCount: Int,
)
