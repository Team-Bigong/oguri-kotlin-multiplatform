package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.CalendarRemoteDataSource
import com.bigong.oguri.data.remote.model.response.CalendarHolidayResponse
import com.bigong.oguri.data.remote.model.response.CalendarPeriodDetailResponse
import com.bigong.oguri.data.remote.model.response.CalendarPeriodResponse
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import com.bigong.oguri.domain.model.CalendarHoliday
import com.bigong.oguri.domain.model.CalendarPeriod
import com.bigong.oguri.domain.model.CalendarPeriodDetail
import com.bigong.oguri.domain.model.CalendarRecommendation
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.repository.CalendarRepository
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class DefaultCalendarRepository(
    private val calendarRemoteDataSource: CalendarRemoteDataSource,
) : CalendarRepository {
    override suspend fun getPreferredDayOffCount(): Int = calendarRemoteDataSource.getPreferredDayOffCount()

    override suspend fun getCalendarRecommendation(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendation =
        calendarRemoteDataSource
            .getCalendarRecommendationResponse(
                year = year,
                month = month,
                dayOffCount = dayOffCount,
            ).toDomain(
                year = year,
                month = month,
            )

    override suspend fun getCalendarPeriodDetail(
        startDate: String,
        endDate: String,
        userCountry: String,
    ): CalendarPeriodDetail =
        calendarRemoteDataSource
            .getCalendarPeriodDetailResponse(
                startDate = startDate,
                endDate = endDate,
                userCountry = userCountry,
            ).toDomain()
}

private fun CalendarRecommendationResponse.toDomain(
    year: Int,
    month: Int,
): CalendarRecommendation =
    CalendarRecommendation(
        leaveDays = dayOffCount,
        year = year,
        month = month,
        holidays = holidays.map { calendarHolidayResponse -> calendarHolidayResponse.toDomain() },
        periods =
            bestPeriods.mapIndexed { index: Int, calendarPeriodResponse: CalendarPeriodResponse ->
                calendarPeriodResponse.toDomain(id = index + 1L)
            },
    )

private fun CalendarHolidayResponse.toDomain(): CalendarHoliday =
    CalendarHoliday(
        date = LocalDate.parse(date),
        name = label,
    )

private fun CalendarPeriodResponse.toDomain(id: Long): CalendarPeriod =
    CalendarPeriod(
        id = id,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
    )

private fun CalendarPeriodDetailResponse.toDomain(): CalendarPeriodDetail =
    CalendarPeriodDetail(
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        holiday = holiday,
        dayOffCount = dayOffCount,
        totalTripCount = totalTripCount,
        places = places.map { placeResponse -> placeResponse.toDomain() },
    )

private fun com.bigong.oguri.data.remote.model.response.PlaceResponse.toDomain(): Place =
    Place(
        id = id,
        country = country,
        city = city,
        summary = summary,
        thumbnailUrl = thumbnailUrl,
        isSaved = saved,
    )
