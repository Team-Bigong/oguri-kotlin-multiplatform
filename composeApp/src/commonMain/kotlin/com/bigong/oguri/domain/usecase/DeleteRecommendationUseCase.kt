package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.HomeRepository
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class DeleteRecommendationUseCase(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate,
        dayOffCount: Int,
    ) {
        homeRepository.deleteRecommendation(
            startDate = startDate,
            endDate = endDate,
            dayOffCount = dayOffCount,
        )
    }
}
