package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberSavedPeriodResponse(
    @SerialName("startDate")
    val startDate: String,
    @SerialName("endDate")
    val endDate: String,
    @SerialName("dayOffCount")
    val dayOffCount: Int,
    @SerialName("totalTripCount")
    val totalTripCount: Int,
)
