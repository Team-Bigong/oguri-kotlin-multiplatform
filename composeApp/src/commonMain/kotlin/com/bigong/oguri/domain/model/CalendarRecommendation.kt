package com.bigong.oguri.domain.model

data class CalendarRecommendation(
    val leaveDays: Int,
    val year: Int,
    val month: Int,
    val holidays: List<CalendarHoliday>,
    val periods: List<CalendarPeriod>,
)
