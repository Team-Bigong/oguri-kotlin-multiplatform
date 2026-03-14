package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarRecommendationResponse(
    @SerialName("dayOffCount")
    val dayOffCount: Int,
    @SerialName("bestPeriods")
    val bestPeriods: List<CalendarPeriodResponse>,
    @SerialName("holidays")
    val holidays: List<CalendarHolidayResponse>,
)
