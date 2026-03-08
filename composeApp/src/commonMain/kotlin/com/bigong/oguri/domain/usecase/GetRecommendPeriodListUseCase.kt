package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.domain.repository.HomeRepository
import dev.zacsweers.metro.Inject

@Inject
class GetRecommendPeriodListUseCase(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(userCountry: String): List<RecommendPeriod> {
        return homeRepository.getRecommendPeriods(userCountry = userCountry)
    }
}
