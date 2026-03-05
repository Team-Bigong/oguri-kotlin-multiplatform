package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse

interface CalendarRemoteDataSource {
    suspend fun getCalendarRecommendationResponse(
        leaveDays: Int,
        year: Int,
        month: Int,
    ): CalendarRecommendationResponse
}
