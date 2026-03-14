package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.CalendarRecommendation
import com.bigong.oguri.domain.repository.CalendarRepository
import dev.zacsweers.metro.Inject

@Inject
class GetCalendarRecommendationUseCase(
    private val calendarRepository: CalendarRepository,
) {
    suspend fun getPreferredDayOffCount(): Int {
        return calendarRepository.getPreferredDayOffCount()
    }

    suspend operator fun invoke(
        year: Int,
        month: Int,
        dayOffCount: Int,
    ): CalendarRecommendation {
        return calendarRepository.getCalendarRecommendation(
            year = year,
            month = month,
            dayOffCount = dayOffCount,
        )
    }
}
