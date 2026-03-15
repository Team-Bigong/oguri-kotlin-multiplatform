package com.bigong.oguri.data.repository

import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.data.remote.AuthRemoteDataSource
import com.bigong.oguri.data.remote.model.request.KakaoLoginRequest
import com.bigong.oguri.domain.repository.AuthRepository
import dev.zacsweers.metro.Inject

@Inject
class DefaultAuthRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun loginWithKakaoAccessToken(kakaoAccessToken: String): String {
        val loginResponse =
            authRemoteDataSource.loginWithKakao(
                request = KakaoLoginRequest(accessToken = kakaoAccessToken),
            )
        AuthTokenStore.updateTokens(
            accessToken = loginResponse.accessToken,
            refreshToken = loginResponse.refreshToken,
        )
        return loginResponse.nickname
    }

    override fun clearTokens() {
        AuthTokenStore.clearTokens()
    }
}
