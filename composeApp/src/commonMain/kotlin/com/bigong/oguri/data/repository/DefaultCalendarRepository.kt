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
    override suspend fun getCalendarRecommendation(
        year: Int,
        month: Int,
        dayOffCount: Int?,
        page: Int,
        size: Int,
    ): CalendarRecommendation =
        calendarRemoteDataSource
            .getCalendarRecommendationResponse(
                year = year,
                month = month,
                dayOffCount = dayOffCount,
                page = page,
                size = size,
            ).toDomain()

    override suspend fun getCalendarPeriodDetail(
        startDate: String,
        endDate: String,
        userCountry: String,
        page: Int,
        size: Int,
    ): CalendarPeriodDetail =
        calendarRemoteDataSource
            .getCalendarPeriodDetailResponse(
                startDate = startDate,
                endDate = endDate,
                userCountry = userCountry,
                page = page,
                size = size,
            ).toDomain()
}

private fun CalendarRecommendationResponse.toDomain(): CalendarRecommendation =
    CalendarRecommendation(
        dayOffCount = dayOffCount,
        page = page,
        size = size,
        hasNext = hasNext,
        periods =
            periods.mapIndexed { index: Int, calendarPeriodResponse: CalendarPeriodResponse ->
                val periodId = (page.toLong() * size.toLong()) + index + 1L
                calendarPeriodResponse.toDomain(id = periodId)
            },
    )

private fun CalendarPeriodResponse.toDomain(id: Long): CalendarPeriod =
    CalendarPeriod(
        id = id,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        totalTripCount = totalTripCount,
        holidayCount = holidayCount,
        dayOffCount = dayOffCount,
        holidayNames = holidays,
        holidayDateDetails = holidayDateDetails.map { holidayResponse -> holidayResponse.toDomain() },
        isSaved = saved ?: isSaved ?: false,
    )

private fun CalendarHolidayResponse.toDomain(): CalendarHoliday =
    CalendarHoliday(
        date = LocalDate.parse(date),
        name = label,
    )

private fun CalendarPeriodDetailResponse.toDomain(): CalendarPeriodDetail =
    CalendarPeriodDetail(
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        holiday = holiday,
        dayOffCount = dayOffCount,
        totalTripCount = totalTripCount,
        page = page,
        size = size,
        hasNext = hasNext,
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
