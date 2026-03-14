package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.CalendarRecommendation

interface CalendarRepository {
    suspend fun getPreferredDayOffCount(): Int

    suspend fun getCalendarRecommendation(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendation
}
