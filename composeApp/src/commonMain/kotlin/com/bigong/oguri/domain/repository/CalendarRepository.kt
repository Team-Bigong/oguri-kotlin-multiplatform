package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.CalendarRecommendation

interface CalendarRepository {
    suspend fun getCalendarRecommendation(
        leaveDays: Int,
        year: Int,
        month: Int,
    ): CalendarRecommendation
}
