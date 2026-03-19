package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.PlaceDetailRepository
import dev.zacsweers.metro.Inject

@Inject
class DeleteSavedDestinationUseCase(
    private val placeDetailRepository: PlaceDetailRepository,
) {
    suspend operator fun invoke(placeId: Long) {
        placeDetailRepository.deleteSavedDestination(placeId = placeId)
    }
}
