package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.AuthRepository
import dev.zacsweers.metro.Inject

@Inject
class LoginWithKakaoAccessTokenUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(kakaoAccessToken: String): Boolean =
        authRepository.loginWithKakaoAccessToken(kakaoAccessToken = kakaoAccessToken)
}
