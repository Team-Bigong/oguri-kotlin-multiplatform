package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.AuthRepository
import dev.zacsweers.metro.Inject

@Inject
class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke() {
        authRepository.clearTokens()
    }
}
