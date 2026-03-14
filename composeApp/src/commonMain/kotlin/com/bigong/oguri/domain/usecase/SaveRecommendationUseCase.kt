package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.HomeRepository
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class SaveRecommendationUseCase(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate,
        dayOffCount: Int,
    ) {
        homeRepository.saveRecommendation(
            startDate = startDate,
            endDate = endDate,
            dayOffCount = dayOffCount,
        )
    }
}
