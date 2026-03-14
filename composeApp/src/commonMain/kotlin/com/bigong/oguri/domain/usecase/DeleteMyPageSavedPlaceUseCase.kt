package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.MyPageInfo
import com.bigong.oguri.domain.repository.MyPageRepository
import dev.zacsweers.metro.Inject

@Inject
class DeleteMyPageSavedPlaceUseCase(
    private val myPageRepository: MyPageRepository,
) {
    suspend operator fun invoke(placeId: Long): MyPageInfo {
        return myPageRepository.deleteSavedPlace(placeId = placeId)
    }
}
