package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.AutoLoginState
import com.bigong.oguri.domain.repository.AuthRepository
import dev.zacsweers.metro.Inject

@Inject
class GetAutoLoginStateUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AutoLoginState = authRepository.getAutoLoginState()
}
