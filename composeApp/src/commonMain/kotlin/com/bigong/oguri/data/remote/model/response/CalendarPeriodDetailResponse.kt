package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarPeriodDetailResponse(
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
    @SerialName("page")
    val page: Int,
    @SerialName("size")
    val size: Int,
    @SerialName("hasNext")
    val hasNext: Boolean,
    @SerialName("places")
    val places: List<PlaceResponse>,
)
