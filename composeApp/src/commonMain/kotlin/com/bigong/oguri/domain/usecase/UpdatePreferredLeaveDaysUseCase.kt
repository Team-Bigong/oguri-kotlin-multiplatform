package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.MyPageInfo
import com.bigong.oguri.domain.repository.MyPageRepository
import dev.zacsweers.metro.Inject

@Inject
class UpdatePreferredLeaveDaysUseCase(
    private val myPageRepository: MyPageRepository,
) {
    suspend operator fun invoke(preferredLeaveDays: Int): MyPageInfo {
        return myPageRepository.updatePreferredLeaveDays(preferredLeaveDays = preferredLeaveDays)
    }
}
