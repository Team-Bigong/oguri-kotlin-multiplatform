package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.MyPageInfo
import com.bigong.oguri.domain.repository.MyPageRepository
import dev.zacsweers.metro.Inject

@Inject
class DeleteMyPageSelectedPeriodUseCase(
    private val myPageRepository: MyPageRepository,
) {
    suspend operator fun invoke(periodId: Long): MyPageInfo {
        return myPageRepository.deleteSelectedPeriod(periodId = periodId)
    }
}
