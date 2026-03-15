package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.request.KakaoLoginRequest
import com.bigong.oguri.data.remote.model.request.RefreshTokenRequest
import com.bigong.oguri.data.remote.model.response.KakaoLoginResponse
import com.bigong.oguri.data.remote.model.response.RefreshTokenResponse

interface AuthRemoteDataSource {
    suspend fun loginWithKakao(request: KakaoLoginRequest): KakaoLoginResponse

    suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse
}
