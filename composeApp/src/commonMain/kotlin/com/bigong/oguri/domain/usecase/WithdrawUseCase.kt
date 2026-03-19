package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.MyPageRepository
import dev.zacsweers.metro.Inject

@Inject
class WithdrawUseCase(
    private val myPageRepository: MyPageRepository,
) {
    suspend operator fun invoke() {
        myPageRepository.withdraw()
    }
}
