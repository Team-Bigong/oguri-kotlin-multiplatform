package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.PlaceDetail
import com.bigong.oguri.domain.repository.PlaceDetailRepository
import dev.zacsweers.metro.Inject

@Inject
class GetPlaceDetailUseCase(
    private val placeDetailRepository: PlaceDetailRepository,
) {
    suspend operator fun invoke(
        placeId: Long,
        startDate: String? = null,
        endDate: String? = null,
        userCountry: String = DEFAULT_USER_COUNTRY,
    ): PlaceDetail =
        placeDetailRepository.getPlaceDetail(
            placeId = placeId,
            startDate = startDate,
            endDate = endDate,
            userCountry = userCountry,
        )

    private companion object {
        private const val DEFAULT_USER_COUNTRY = "대한민국"
    }
}
