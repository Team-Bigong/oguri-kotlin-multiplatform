package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.PlaceDetailRepository
import dev.zacsweers.metro.Inject

@Inject
class SaveDestinationUseCase(
    private val placeDetailRepository: PlaceDetailRepository,
) {
    suspend operator fun invoke(placeId: Long) {
        placeDetailRepository.saveDestination(placeId = placeId)
    }
}
