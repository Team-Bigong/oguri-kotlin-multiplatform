package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse

interface CalendarRemoteDataSource {
    suspend fun getPreferredDayOffCount(): Int

    suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendationResponse
}
