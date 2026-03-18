package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.CalendarHolidayResponse
import com.bigong.oguri.data.remote.model.response.CalendarPeriodDetailResponse
import com.bigong.oguri.data.remote.model.response.CalendarPeriodResponse
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import com.bigong.oguri.data.remote.model.response.PlaceResponse
import dev.zacsweers.metro.Inject
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus

@Inject
class KtorCalendarRemoteDataSource : CalendarRemoteDataSource {
    override suspend fun getPreferredDayOffCount(): Int = 3

    override suspend fun getCalendarRecommendationResponse(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendationResponse {
        val baseDate = LocalDate.parse("$year-${month.toString().padStart(2, '0')}-01")
        val periodResponses =
            buildPeriodResponses(
                baseDate = baseDate,
                dayOffCount = dayOffCount,
            )

        val holidayResponses =
            buildHolidayResponses(
                baseDate = baseDate,
            )

        return CalendarRecommendationResponse(
            dayOffCount = dayOffCount,
            bestPeriods = periodResponses,
            holidays = holidayResponses,
        )
    }

    override suspend fun getCalendarPeriodDetailResponse(
        startDate: String,
        endDate: String,
        userCountry: String,
    ): CalendarPeriodDetailResponse {
        val startLocalDate = LocalDate.parse(startDate)
        val endLocalDate = LocalDate.parse(endDate)
        val totalTripCount = (endLocalDate.toEpochDays() - startLocalDate.toEpochDays() + 1).toInt()
        val dayOffCount = (totalTripCount - 3).coerceAtLeast(1)

        return CalendarPeriodDetailResponse(
            startDate = startDate,
            endDate = endDate,
            holiday = listOf("삼일절", "대체휴일"),
            dayOffCount = dayOffCount,
            totalTripCount = totalTripCount,
            places =
                listOf(
                    PlaceResponse(
                        id = 1L,
                        country = "필리핀",
                        city = "보라카이",
                        summary = "화이트 비치 물빛이 가장 또렷해지는 시기예요",
                        thumbnailUrl = PLACE_IMAGE_BORACAY,
                        saved = true,
                    ),
                    PlaceResponse(
                        id = 2L,
                        country = "스페인",
                        city = "바르셀로나",
                        summary = "가우디 건축과 바다 산책을 함께 즐기기 좋아요",
                        thumbnailUrl = PLACE_IMAGE_BARCELONA,
                        saved = false,
                    ),
                    PlaceResponse(
                        id = 3L,
                        country = "미국",
                        city = "샌프란시스코",
                        summary = "언덕과 바다 풍경이 가장 또렷해지는 시기예요",
                        thumbnailUrl = PLACE_IMAGE_SAN_FRANCISCO,
                        saved = true,
                    ),
                    PlaceResponse(
                        id = 4L,
                        country = "일본",
                        city = "오사카",
                        summary = "가볍게 떠나기 좋은 근거리 일정이에요",
                        thumbnailUrl = PLACE_IMAGE_BORACAY,
                        saved = false,
                    ),
                ),
        )
    }

    private fun buildPeriodResponses(
        baseDate: LocalDate,
        dayOffCount: Int,
    ): List<CalendarPeriodResponse> {
        fun toPeriodResponse(
            startDate: LocalDate,
            totalTripCount: Int,
        ): CalendarPeriodResponse {
            val endDate = startDate.plus(totalTripCount - 1, DateTimeUnit.DAY)
            return CalendarPeriodResponse(
                startDate = startDate.toString(),
                endDate = endDate.toString(),
            )
        }

        val periodSeeds =
            when (baseDate.year to baseDate.month) {
                2026 to Month.MARCH ->
                    listOf(
                        baseDate.plus(2, DateTimeUnit.DAY) to (dayOffCount + 2),
                        baseDate.plus(8, DateTimeUnit.DAY) to (dayOffCount + 4),
                        baseDate.plus(14, DateTimeUnit.DAY) to (dayOffCount + 8),
                        baseDate.plus(21, DateTimeUnit.DAY) to (dayOffCount + 3),
                    )

                2026 to Month.DECEMBER ->
                    listOf(
                        LocalDate(2026, Month.DECEMBER, 24) to 12,
                        LocalDate(2026, Month.DECEMBER, 30) to 7,
                        LocalDate(2026, Month.DECEMBER, 10) to (dayOffCount + 4),
                        LocalDate(2026, Month.DECEMBER, 17) to (dayOffCount + 3),
                    )

                2027 to Month.JANUARY ->
                    listOf(
                        LocalDate(2026, Month.DECEMBER, 31) to 9,
                        LocalDate(2027, Month.JANUARY, 13) to (dayOffCount + 5),
                        LocalDate(2027, Month.JANUARY, 21) to (dayOffCount + 3),
                        LocalDate(2027, Month.JANUARY, 28) to 10,
                    )

                else ->
                    listOf(
                        baseDate.plus(1, DateTimeUnit.DAY) to (dayOffCount + 2),
                        baseDate.plus(7, DateTimeUnit.DAY) to (dayOffCount + 3),
                        baseDate.plus(13, DateTimeUnit.DAY) to (dayOffCount + 5),
                        baseDate.plus(20, DateTimeUnit.DAY) to (dayOffCount + 2),
                    )
            }

        return periodSeeds.map { (startDate, totalTripCount) ->
            toPeriodResponse(startDate = startDate, totalTripCount = totalTripCount.coerceAtLeast(dayOffCount + 1))
        }
    }

    private fun buildHolidayResponses(baseDate: LocalDate): List<CalendarHolidayResponse> {
        val publicHolidayByYearMonth =
            mapOf(
                (2026 to 1) to listOf(1 to "신정"),
                (2026 to 3) to listOf(1 to "삼일절", 2 to "대체휴일"),
                (2026 to 5) to listOf(5 to "어린이날"),
                (2026 to 6) to listOf(6 to "현충일"),
                (2026 to 8) to listOf(15 to "광복절"),
                (2026 to 10) to listOf(3 to "개천절", 9 to "한글날"),
                (2026 to 12) to listOf(25 to "성탄절"),
                (2027 to 1) to listOf(1 to "신정"),
                (2027 to 3) to listOf(1 to "삼일절"),
            )

        val thisMonthHolidays =
            publicHolidayByYearMonth[baseDate.year to (baseDate.month.ordinal + 1)]
                .orEmpty()
                .mapNotNull { (day, label) ->
                    runCatching {
                        CalendarHolidayResponse(
                            date = LocalDate(baseDate.year, baseDate.month, day).toString(),
                            label = label,
                        )
                    }.getOrNull()
                }

        return thisMonthHolidays.distinctBy { holidayResponse -> holidayResponse.date }
    }

    private companion object {
        private const val PLACE_IMAGE_BORACAY =
            "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg"
        private const val PLACE_IMAGE_BARCELONA =
            "https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/destination%2Fimg_barcelona.jpg?alt=media"
        private const val PLACE_IMAGE_SAN_FRANCISCO =
            "https://firebasestorage.googleapis.com/v0/b/oguri-74e46.firebasestorage.app/o/destination%2Fimg_san_francisco.jpg?alt=media"
    }
}
