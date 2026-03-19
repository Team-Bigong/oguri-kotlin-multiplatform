package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.CalendarPeriodDetail
import com.bigong.oguri.domain.model.CalendarRecommendation

interface CalendarRepository {
    suspend fun getCalendarRecommendation(
        year: Int,
        month: Int,
        dayOffCount: Int?,
        page: Int,
        size: Int,
    ): CalendarRecommendation

    suspend fun getCalendarPeriodDetail(
        startDate: String,
        endDate: String,
        userCountry: String,
        page: Int,
        size: Int,
    ): CalendarPeriodDetail
}
