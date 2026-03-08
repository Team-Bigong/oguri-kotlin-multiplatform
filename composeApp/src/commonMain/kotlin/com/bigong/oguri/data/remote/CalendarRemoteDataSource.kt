package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse

interface CalendarRemoteDataSource {
    suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
    ): CalendarRecommendationResponse

    suspend fun updateMemberDayOffCount(dayOffCount: Int): Int
}
