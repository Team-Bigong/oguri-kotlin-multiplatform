package com.bigong.oguri.dto

data class AdminLoginRequest(
    val username: String,
    val password: String
)

data class AdminLoginResponse(
    val accessToken: String,
    val expiresInSeconds: Long
)
