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
        month: Int,
    ): CalendarRecommendation {
        return calendarRepository.getCalendarRecommendation(
            year = year,
            month = month,
        )
    }

    suspend fun updateDayOffCount(dayOffCount: Int): Int {
        return calendarRepository.updateMemberDayOffCount(dayOffCount = dayOffCount)
    }
}
