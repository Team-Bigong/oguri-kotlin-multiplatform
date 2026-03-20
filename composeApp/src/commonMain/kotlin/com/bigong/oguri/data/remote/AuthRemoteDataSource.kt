package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.request.AppleLoginRequest
import com.bigong.oguri.data.remote.model.request.GoogleLoginRequest
import com.bigong.oguri.data.remote.model.request.KakaoLoginRequest
import com.bigong.oguri.data.remote.model.request.RefreshTokenRequest
import com.bigong.oguri.data.remote.model.request.UpdateMemberDayOffRequest
import com.bigong.oguri.data.remote.model.response.AuthLoginResponse
import com.bigong.oguri.data.remote.model.response.MemberMeResponse
import com.bigong.oguri.data.remote.model.response.RefreshTokenResponse

interface AuthRemoteDataSource {
    suspend fun loginWithKakao(request: KakaoLoginRequest): AuthLoginResponse

    suspend fun loginWithGoogle(request: GoogleLoginRequest): AuthLoginResponse

    suspend fun loginWithApple(request: AppleLoginRequest): AuthLoginResponse

    suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse

    suspend fun getMemberMe(): MemberMeResponse

    suspend fun completeOnboarding(request: UpdateMemberDayOffRequest)
}
