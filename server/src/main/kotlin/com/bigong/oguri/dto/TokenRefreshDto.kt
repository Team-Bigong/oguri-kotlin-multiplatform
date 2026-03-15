package com.bigong.oguri.dto

/**
 * 토큰 재발급 요청
 */
data class TokenRefreshRequest(
    val refreshToken: String
)

/**
 * 토큰 재발급 응답
 */
data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String
)
