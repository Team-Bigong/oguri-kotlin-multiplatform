package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.AutoLoginState

interface AuthRepository {
    suspend fun loginWithKakaoAccessToken(kakaoAccessToken: String): Boolean

    suspend fun loginWithAppleIdentityToken(identityToken: String): Boolean

    suspend fun getAutoLoginState(): AutoLoginState

    suspend fun completeOnboarding(
        preferredDayOff: Int,
        remainingDayOff: Int,
    )

    fun clearTokens()
}
