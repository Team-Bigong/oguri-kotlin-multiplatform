package com.bigong.oguri.dto.response

/**
 * 토큰 재발급 응답
 */
data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)
