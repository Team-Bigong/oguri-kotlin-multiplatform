package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.CalendarPeriodDetail
import com.bigong.oguri.domain.repository.CalendarRepository
import dev.zacsweers.metro.Inject

@Inject
class GetCalendarPeriodDetailUseCase(
    private val calendarRepository: CalendarRepository,
) {
    suspend operator fun invoke(
        startDate: String,
        endDate: String,
        userCountry: String = DEFAULT_USER_COUNTRY,
    ): CalendarPeriodDetail =
        calendarRepository.getCalendarPeriodDetail(
            startDate = startDate,
            endDate = endDate,
            userCountry = userCountry,
        )

    private companion object {
        private const val DEFAULT_USER_COUNTRY = "대한민국"
    }
}
