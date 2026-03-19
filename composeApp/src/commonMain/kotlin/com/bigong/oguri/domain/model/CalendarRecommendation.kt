package com.bigong.oguri.domain.model

data class CalendarRecommendation(
    val dayOffCount: Int,
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    val periods: List<CalendarPeriod>,
)
