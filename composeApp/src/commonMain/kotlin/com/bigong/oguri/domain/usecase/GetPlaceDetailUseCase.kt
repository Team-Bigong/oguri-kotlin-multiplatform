package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.PlaceDetail
import com.bigong.oguri.domain.repository.PlaceDetailRepository
import dev.zacsweers.metro.Inject

@Inject
class GetPlaceDetailUseCase(
    private val placeDetailRepository: PlaceDetailRepository,
) {
    suspend operator fun invoke(placeId: Long): PlaceDetail {
        return placeDetailRepository.getPlaceDetail(placeId = placeId)
    }
}
