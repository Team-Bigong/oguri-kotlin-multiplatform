package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.RecommendationSavedChange
import com.bigong.oguri.domain.repository.HomeRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
class ObserveRecommendationSavedChangesUseCase(
    private val homeRepository: HomeRepository,
) {
    operator fun invoke(): Flow<RecommendationSavedChange> = homeRepository.observeRecommendationSavedChanges()
}
