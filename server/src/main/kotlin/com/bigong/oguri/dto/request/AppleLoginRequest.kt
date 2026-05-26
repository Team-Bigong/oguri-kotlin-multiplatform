package com.bigong.oguri.dto.request

/**
 * 클라이언트로부터 받는 Apple 로그인 요청
 */
data class AppleLoginRequest(
    val identityToken: String,
)
