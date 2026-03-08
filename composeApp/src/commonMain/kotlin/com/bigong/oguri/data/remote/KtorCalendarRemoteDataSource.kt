package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.request.UpdateMemberDayOffRequest
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

@Inject
class KtorCalendarRemoteDataSource(
    private val httpClient: HttpClient,
) : CalendarRemoteDataSource {
    override suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
    ): CalendarRecommendationResponse {
        val requestUrl = "$DEBUG_BASE_URL$CALENDAR_API_PATH"
        val yearMonth = "${year}-${month.toString().padStart(2, '0')}"
        return httpClient.get(requestUrl) {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            parameter(CALENDAR_YEAR_MONTH_QUERY_NAME, yearMonth)
        }.body()
    }

    override suspend fun updateMemberDayOffCount(dayOffCount: Int): Int {
        val requestUrl = "$DEBUG_BASE_URL$MEMBER_DAY_OFF_API_PATH"
        return httpClient.post(requestUrl) {
            header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            setBody(UpdateMemberDayOffRequest(dayOffCount = dayOffCount))
        }.body()
    }

    private companion object {
        private const val CALENDAR_API_PATH: String = "/api/v1/calendar"
        private const val MEMBER_DAY_OFF_API_PATH: String = "/api/v1/members/day-off"
        private const val USER_ID_HEADER_NAME: String = "X-USER-ID"
        private const val DEFAULT_USER_ID: String = "GUEST"
        private const val CALENDAR_YEAR_MONTH_QUERY_NAME: String = "yearMonth"
    }
}
