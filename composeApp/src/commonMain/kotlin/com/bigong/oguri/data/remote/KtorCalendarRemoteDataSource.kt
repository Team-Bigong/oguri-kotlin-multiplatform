package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import com.bigong.oguri.data.remote.model.response.MemberDayOffResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

@Inject
class KtorCalendarRemoteDataSource(
    private val httpClient: HttpClient,
) : CalendarRemoteDataSource {
    override suspend fun getMemberDayOffResponse(): MemberDayOffResponse {
        val requestUrl = "$DEBUG_BASE_URL$MEMBER_DAY_OFF_API_PATH"
        return httpClient.get(requestUrl) {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
        }.body()
    }

    override suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendationResponse {
        val requestUrl = "$DEBUG_BASE_URL$CALENDAR_API_PATH"
        val yearMonth = "${year}-${month.toString().padStart(2, '0')}"
        return httpClient.get(requestUrl) {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            parameter(CALENDAR_YEAR_MONTH_QUERY_NAME, yearMonth)
            parameter(CALENDAR_DAY_OFF_COUNT_QUERY_NAME, dayOffCount)
        }.body()
    }

    private companion object {
        private const val CALENDAR_API_PATH: String = "/api/v1/calendar"
        private const val MEMBER_DAY_OFF_API_PATH: String = "/api/v1/members/day-off"
        private const val USER_ID_HEADER_NAME: String = "X-USER-ID"
        private const val DEFAULT_USER_ID: String = "GUEST"
        private const val CALENDAR_YEAR_MONTH_QUERY_NAME: String = "yearMonth"
        private const val CALENDAR_DAY_OFF_COUNT_QUERY_NAME: String = "dayOffCount"
    }
}
