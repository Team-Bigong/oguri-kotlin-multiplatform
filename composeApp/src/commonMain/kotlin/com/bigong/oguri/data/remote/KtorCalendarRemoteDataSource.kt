package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.request.GetCalendarRecommendationRequest
import com.bigong.oguri.data.remote.model.response.CalendarHolidayResponse
import com.bigong.oguri.data.remote.model.response.CalendarPeriodResponse
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlinx.coroutines.delay

@Inject
class KtorCalendarRemoteDataSource(
    private val httpClient: HttpClient,
) : CalendarRemoteDataSource {
    override suspend fun getCalendarRecommendationResponse(
        leaveDays: Int,
        year: Int,
        month: Int,
    ): CalendarRecommendationResponse {
        val request =
            GetCalendarRecommendationRequest(
                leaveDays = leaveDays,
                year = year,
                month = month,
            )

        val simulatedRequestUrlBuilder =
            URLBuilder().apply {
                takeFrom("https://api.oguri.app/v1/calendar/recommendation")
                parameters.append("leaveDays", request.leaveDays.toString())
                parameters.append("year", request.year.toString())
                parameters.append("month", request.month.toString())
            }

        if (simulatedRequestUrlBuilder.host.isEmpty() || httpClient.hashCode() == 0) {
            return CalendarRecommendationResponse(
                leaveDays = leaveDays,
                year = year,
                month = month,
                holidays = emptyList(),
                periods = emptyList(),
            )
        }

        delay(260)

        return createDummyCalendarRecommendationResponse(
            leaveDays = leaveDays,
            year = year,
            month = month,
        )
    }

    private fun createDummyCalendarRecommendationResponse(
        leaveDays: Int,
        year: Int,
        month: Int,
    ): CalendarRecommendationResponse {
        val normalizedLeaveDays = leaveDays.coerceIn(minimumValue = 1, maximumValue = 30)

        val holidays =
            when {
                year == 2026 && month == 3 -> {
                    listOf(
                        CalendarHolidayResponse(date = "2026-03-01", name = "삼일절"),
                        CalendarHolidayResponse(date = "2026-03-02", name = "대체휴일"),
                    )
                }

                else -> {
                    listOf(
                        CalendarHolidayResponse(date = "$year-${month.toString().padStart(length = 2, padChar = '0')}-01", name = "공휴일"),
                    )
                }
            }

        val periods =
            if (year == 2026 && month == 3) {
                listOf(
                    CalendarPeriodResponse(id = 1L, startDate = "2026-02-28", endDate = "2026-03-04"),
                    CalendarPeriodResponse(id = 2L, startDate = "2026-03-07", endDate = "2026-03-10"),
                    CalendarPeriodResponse(id = 3L, startDate = "2026-03-12", endDate = "2026-03-15"),
                )
            } else {
                createGenericPeriods(
                    year = year,
                    month = month,
                    leaveDays = normalizedLeaveDays,
                )
            }

        return CalendarRecommendationResponse(
            leaveDays = normalizedLeaveDays,
            year = year,
            month = month,
            holidays = holidays,
            periods = periods,
        )
    }

    private fun createGenericPeriods(
        year: Int,
        month: Int,
        leaveDays: Int,
    ): List<CalendarPeriodResponse> {
        val monthText = month.toString().padStart(length = 2, padChar = '0')
        val firstEndDay = (leaveDays + 1).coerceAtMost(8)
        val secondStartDay = (firstEndDay + 3).coerceAtMost(22)
        val secondEndDay = (secondStartDay + leaveDays).coerceAtMost(26)
        val thirdStartDay = (secondEndDay + 2).coerceAtMost(28)
        val thirdEndDay = (thirdStartDay + leaveDays).coerceAtMost(30)

        return listOf(
            CalendarPeriodResponse(
                id = 1L,
                startDate = "$year-$monthText-01",
                endDate = "$year-$monthText-${firstEndDay.toString().padStart(length = 2, padChar = '0')}",
            ),
            CalendarPeriodResponse(
                id = 2L,
                startDate = "$year-$monthText-${secondStartDay.toString().padStart(length = 2, padChar = '0')}",
                endDate = "$year-$monthText-${secondEndDay.toString().padStart(length = 2, padChar = '0')}",
            ),
            CalendarPeriodResponse(
                id = 3L,
                startDate = "$year-$monthText-${thirdStartDay.toString().padStart(length = 2, padChar = '0')}",
                endDate = "$year-$monthText-${thirdEndDay.toString().padStart(length = 2, padChar = '0')}",
            ),
        )
    }
}
