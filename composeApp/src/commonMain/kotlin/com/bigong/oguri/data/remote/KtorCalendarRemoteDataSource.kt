package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.response.CalendarPeriodDetailResponse
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import com.bigong.oguri.data.remote.model.response.MemberDayOffResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

@Inject
class KtorCalendarRemoteDataSource(
    private val httpClient: HttpClient,
    private val authRequestExecutor: AuthRequestExecutor,
) : CalendarRemoteDataSource {
    override suspend fun getPreferredDayOffCount(): Int {
        val requestUrl = "$DEBUG_BASE_URL$MEMBER_DAY_OFF_API_PATH"
        val response =
            authRequestExecutor.execute {
                httpClient
                    .get(requestUrl)
                    .body<MemberDayOffResponse>()
            }
        return response.preferredDayOff
    }

    override suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendationResponse {
        val requestUrl = "$DEBUG_BASE_URL$CALENDAR_API_PATH"
        val yearMonth = "${year}-${month.toString().padStart(2, '0')}"
        return authRequestExecutor.execute {
            httpClient
                .get(requestUrl) {
                    parameter(CALENDAR_YEAR_MONTH_QUERY_NAME, yearMonth)
                    parameter(CALENDAR_DAY_OFF_COUNT_QUERY_NAME, dayOffCount)
                }.body()
        }
    }

    override suspend fun getCalendarPeriodDetailResponse(
        startDate: String,
        endDate: String,
        userCountry: String,
    ): CalendarPeriodDetailResponse {
        val requestUrl = "$DEBUG_BASE_URL$CALENDAR_DETAIL_API_PATH"
        return authRequestExecutor.execute {
            httpClient
                .get(requestUrl) {
                    parameter(CALENDAR_DETAIL_START_DATE_QUERY_NAME, startDate)
                    parameter(CALENDAR_DETAIL_END_DATE_QUERY_NAME, endDate)
                    parameter(CALENDAR_DETAIL_USER_COUNTRY_QUERY_NAME, userCountry)
                }.body()
        }
    }

    private companion object {
        private const val CALENDAR_API_PATH = "/api/v1/calendar"
        private const val CALENDAR_DETAIL_API_PATH = "/api/v1/calendar/detail"
        private const val MEMBER_DAY_OFF_API_PATH = "/api/v1/members/day-off"
        private const val CALENDAR_YEAR_MONTH_QUERY_NAME = "yearMonth"
        private const val CALENDAR_DAY_OFF_COUNT_QUERY_NAME = "dayOffCount"
        private const val CALENDAR_DETAIL_START_DATE_QUERY_NAME = "startDate"
        private const val CALENDAR_DETAIL_END_DATE_QUERY_NAME = "endDate"
        private const val CALENDAR_DETAIL_USER_COUNTRY_QUERY_NAME = "userCountry"
    }
}
