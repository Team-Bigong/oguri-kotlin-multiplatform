package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.CalendarRecommendation
import com.bigong.oguri.domain.repository.CalendarRepository
import dev.zacsweers.metro.Inject

@Inject
class GetCalendarRecommendationUseCase(
    private val calendarRepository: CalendarRepository,
) {
    suspend operator fun invoke(
        year: Int,
        month: Int?,
        dayOffCount: Int?,
        page: Int,
        size: Int,
    ): CalendarRecommendation =
        calendarRepository.getCalendarRecommendation(
            year = year,
            month = month,
            dayOffCount = dayOffCount,
            page = page,
            size = size,
        )
}
