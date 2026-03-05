package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.CalendarRecommendation
import com.bigong.oguri.domain.repository.CalendarRepository
import dev.zacsweers.metro.Inject

@Inject
class GetCalendarRecommendationUseCase(
    private val calendarRepository: CalendarRepository,
) {
    suspend operator fun invoke(
        leaveDays: Int,
        year: Int,
        month: Int,
    ): CalendarRecommendation {
        return calendarRepository.getCalendarRecommendation(
            leaveDays = leaveDays,
            year = year,
            month = month,
        )
    }
}
