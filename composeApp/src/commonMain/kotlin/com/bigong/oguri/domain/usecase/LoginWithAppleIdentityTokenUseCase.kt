package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.AuthRepository
import dev.zacsweers.metro.Inject

@Inject
class LoginWithAppleIdentityTokenUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(identityToken: String): Boolean = authRepository.loginWithAppleIdentityToken(identityToken = identityToken)
}
