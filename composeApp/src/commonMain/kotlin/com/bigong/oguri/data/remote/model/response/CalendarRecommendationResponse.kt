package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class CalendarRecommendationResponse(
    val leaveDays: Int,
    val year: Int,
    val month: Int,
    val holidays: List<CalendarHolidayResponse>,
    val periods: List<CalendarPeriodResponse>,
)
