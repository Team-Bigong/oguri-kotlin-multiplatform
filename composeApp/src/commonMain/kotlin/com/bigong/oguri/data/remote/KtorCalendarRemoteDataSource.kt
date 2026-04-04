package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.BASE_URL
import com.bigong.oguri.data.remote.model.response.CalendarPeriodDetailResponse
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
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
    override suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int?,
        dayOffCount: Int?,
        page: Int,
        size: Int,
    ): CalendarRecommendationResponse {
        val requestUrl = "$BASE_URL$CALENDAR_API_PATH"
        return authRequestExecutor.execute {
            httpClient
                .get(requestUrl) {
                    parameter(CALENDAR_YEAR_QUERY_NAME, year)
                    month?.let { resolvedMonth: Int ->
                        parameter(CALENDAR_MONTH_QUERY_NAME, resolvedMonth)
                    }
                    dayOffCount?.let { resolvedDayOffCount ->
                        parameter(CALENDAR_DAY_OFF_COUNT_QUERY_NAME, resolvedDayOffCount)
                    }
                    parameter(CALENDAR_PAGE_QUERY_NAME, page)
                    parameter(CALENDAR_SIZE_QUERY_NAME, size)
                }.body()
        }
    }

    override suspend fun getCalendarPeriodDetailResponse(
        startDate: String,
        endDate: String,
        userCountry: String,
        page: Int,
        size: Int,
    ): CalendarPeriodDetailResponse {
        val requestUrl = "$BASE_URL$CALENDAR_DETAIL_API_PATH"
        return authRequestExecutor.execute {
            httpClient
                .get(requestUrl) {
                    parameter(CALENDAR_DETAIL_START_DATE_QUERY_NAME, startDate)
                    parameter(CALENDAR_DETAIL_END_DATE_QUERY_NAME, endDate)
                    parameter(CALENDAR_DETAIL_USER_COUNTRY_QUERY_NAME, userCountry)
                    parameter(CALENDAR_PAGE_QUERY_NAME, page)
                    parameter(CALENDAR_SIZE_QUERY_NAME, size)
                }.body()
        }
    }

    private companion object {
        private const val CALENDAR_API_PATH = "/api/v1/calendar"
        private const val CALENDAR_DETAIL_API_PATH = "/api/v1/calendar/detail"
        private const val CALENDAR_YEAR_QUERY_NAME = "year"
        private const val CALENDAR_MONTH_QUERY_NAME = "month"
        private const val CALENDAR_DAY_OFF_COUNT_QUERY_NAME = "dayOffCount"
        private const val CALENDAR_PAGE_QUERY_NAME = "page"
        private const val CALENDAR_SIZE_QUERY_NAME = "size"
        private const val CALENDAR_DETAIL_START_DATE_QUERY_NAME = "startDate"
        private const val CALENDAR_DETAIL_END_DATE_QUERY_NAME = "endDate"
        private const val CALENDAR_DETAIL_USER_COUNTRY_QUERY_NAME = "userCountry"
    }
}
