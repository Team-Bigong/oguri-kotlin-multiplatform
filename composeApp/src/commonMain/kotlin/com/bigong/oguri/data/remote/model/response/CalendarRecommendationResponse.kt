package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarRecommendationResponse(
    @SerialName("dayOffCount")
    val dayOffCount: Int,
    @SerialName("page")
    val page: Int,
    @SerialName("size")
    val size: Int,
    @SerialName("hasNext")
    val hasNext: Boolean,
    @SerialName("periods")
    val periods: List<CalendarPeriodResponse>,
)
