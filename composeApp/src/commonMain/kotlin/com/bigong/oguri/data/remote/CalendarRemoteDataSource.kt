package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.CalendarPeriodDetailResponse
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse

interface CalendarRemoteDataSource {
    suspend fun getPreferredDayOffCount(): Int

    suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendationResponse

    suspend fun getCalendarPeriodDetailResponse(
        startDate: String,
        endDate: String,
        userCountry: String,
    ): CalendarPeriodDetailResponse
}
