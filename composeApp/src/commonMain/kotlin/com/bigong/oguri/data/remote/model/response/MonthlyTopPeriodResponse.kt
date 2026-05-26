package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MonthlyTopPeriodResponse(
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("totalTripCount")
    val totalTripCount: Int,
    @SerialName("holidayCount")
    val holidayCount: Int,
    @SerialName("dayOffCount")
    val dayOffCount: Int,
)
