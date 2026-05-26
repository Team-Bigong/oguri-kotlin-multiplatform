package com.bigong.oguri.dto.request

/**
 * 토큰 재발급 요청
 */
data class TokenRefreshRequest(
    val refreshToken: String,
)
