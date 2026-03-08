package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import com.bigong.oguri.data.remote.model.response.MemberDayOffResponse

interface CalendarRemoteDataSource {
    suspend fun getMemberDayOffResponse(): MemberDayOffResponse

    suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendationResponse
}
