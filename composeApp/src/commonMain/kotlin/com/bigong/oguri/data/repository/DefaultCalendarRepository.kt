package com.bigong.oguri.data.repository

import com.bigong.oguri.data.remote.CalendarRemoteDataSource
import com.bigong.oguri.data.remote.model.response.CalendarHolidayResponse
import com.bigong.oguri.data.remote.model.response.CalendarPeriodResponse
import com.bigong.oguri.data.remote.model.response.CalendarRecommendationResponse
import com.bigong.oguri.domain.model.CalendarHoliday
import com.bigong.oguri.domain.model.CalendarPeriod
import com.bigong.oguri.domain.model.CalendarRecommendation
import com.bigong.oguri.domain.repository.CalendarRepository
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class DefaultCalendarRepository(
    private val calendarRemoteDataSource: CalendarRemoteDataSource,
) : CalendarRepository {
    override suspend fun getCalendarRecommendation(
        leaveDays: Int,
        year: Int,
        month: Int,
    ): CalendarRecommendation {
        return calendarRemoteDataSource.getCalendarRecommendationResponse(
            leaveDays = leaveDays,
            year = year,
            month = month,
        ).toDomain()
    }
}

private fun CalendarRecommendationResponse.toDomain(): CalendarRecommendation {
    return CalendarRecommendation(
        leaveDays = leaveDays,
        year = year,
        month = month,
        holidays = holidays.map { calendarHolidayResponse: CalendarHolidayResponse -> calendarHolidayResponse.toDomain() },
        periods = periods.map { calendarPeriodResponse: CalendarPeriodResponse -> calendarPeriodResponse.toDomain() },
    )
}

private fun CalendarHolidayResponse.toDomain(): CalendarHoliday {
    return CalendarHoliday(
        date = LocalDate.parse(date),
        name = name,
    )
}

private fun CalendarPeriodResponse.toDomain(): CalendarPeriod {
    return CalendarPeriod(
        id = id,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
    )
}
