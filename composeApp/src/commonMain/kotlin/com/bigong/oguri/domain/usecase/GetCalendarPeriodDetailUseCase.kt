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
        page: Int = DEFAULT_PAGE,
        size: Int = DEFAULT_PAGE_SIZE,
    ): CalendarPeriodDetail =
        calendarRepository.getCalendarPeriodDetail(
            startDate = startDate,
            endDate = endDate,
            userCountry = userCountry,
            page = page,
            size = size,
        )

    private companion object {
        private const val DEFAULT_USER_COUNTRY = "대한민국"
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_PAGE_SIZE = 10
    }
}
