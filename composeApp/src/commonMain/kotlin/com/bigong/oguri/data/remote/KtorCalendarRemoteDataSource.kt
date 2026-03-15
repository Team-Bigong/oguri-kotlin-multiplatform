package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.response.CalendarPeriodDetailResponse
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import com.bigong.oguri.data.remote.model.response.MemberMeResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

@Inject
class KtorCalendarRemoteDataSource(
    private val httpClient: HttpClient,
    private val authRequestExecutor: AuthRequestExecutor,
) : CalendarRemoteDataSource {
    override suspend fun getPreferredDayOffCount(): Int {
        val requestUrl = "$DEBUG_BASE_URL$MEMBER_ME_API_PATH"
        return authRequestExecutor.execute {
            httpClient.get(requestUrl) {
                header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
            }.body<MemberMeResponse>().preferredDayOff
        }
    }

    override suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendationResponse {
        val requestUrl = "$DEBUG_BASE_URL$CALENDAR_API_PATH"
        val yearMonth = "${year}-${month.toString().padStart(2, '0')}"
        return authRequestExecutor.execute {
            httpClient.get(requestUrl) {
                header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
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
            httpClient.get(requestUrl) {
                header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
                parameter(CALENDAR_DETAIL_START_DATE_QUERY_NAME, startDate)
                parameter(CALENDAR_DETAIL_END_DATE_QUERY_NAME, endDate)
                parameter(USER_COUNTRY_QUERY_NAME, userCountry)
            }.body()
        }
    }

    private companion object {
        private const val CALENDAR_API_PATH: String = "/api/v1/calendar"
        private const val CALENDAR_DETAIL_API_PATH: String = "/api/v1/calendar/detail"
        private const val MEMBER_ME_API_PATH: String = "/api/v1/members/me"
        private const val USER_ID_HEADER_NAME: String = "X-USER-ID"
        private const val DEFAULT_USER_ID: String = "GUEST"
        private const val CALENDAR_YEAR_MONTH_QUERY_NAME: String = "yearMonth"
        private const val CALENDAR_DAY_OFF_COUNT_QUERY_NAME: String = "dayOffCount"
        private const val CALENDAR_DETAIL_START_DATE_QUERY_NAME: String = "startDate"
        private const val CALENDAR_DETAIL_END_DATE_QUERY_NAME: String = "endDate"
        private const val USER_COUNTRY_QUERY_NAME: String = "userCountry"
    }
}
