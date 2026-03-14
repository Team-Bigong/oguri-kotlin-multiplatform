package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyPageSelectedPeriodResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("totalTripCount")
    val totalTripCount: Int,
    @SerialName("dayOffCount")
    val dayOffCount: Int,
)
