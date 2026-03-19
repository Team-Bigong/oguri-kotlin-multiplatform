package com.bigong.oguri.dto

import io.swagger.v3.oas.annotations.media.Schema

data class LoginResponse(
    @field:Schema(description = "서비스 액세스 토큰")
    val accessToken: String,
    @field:Schema(description = "서비스 리프레시 토큰")
    val refreshToken: String,
    @field:Schema(description = "사용자 닉네임")
    val nickname: String,
    @field:Schema(description = "온보딩 완료 여부. false면 온보딩 화면 진입 필요", example = "false")
    val onboardingCompleted: Boolean
)
