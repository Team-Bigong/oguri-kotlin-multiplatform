package com.bigong.oguri.domain.repository

interface AuthRepository {
    suspend fun loginWithKakaoAccessToken(kakaoAccessToken: String): String

    fun clearTokens()
}
