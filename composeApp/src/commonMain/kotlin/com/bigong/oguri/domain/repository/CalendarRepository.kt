package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.CalendarRecommendation

interface CalendarRepository {
    suspend fun getCalendarRecommendation(
        year: Int,
        month: Int,
    ): CalendarRecommendation

    suspend fun updateMemberDayOffCount(dayOffCount: Int): Int
}
