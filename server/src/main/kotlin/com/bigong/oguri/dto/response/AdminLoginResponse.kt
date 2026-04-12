package com.bigong.oguri.dto.response

data class AdminLoginResponse(
    val accessToken: String,
    val expiresInSeconds: Long,
)
