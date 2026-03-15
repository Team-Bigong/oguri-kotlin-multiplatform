package com.bigong.oguri.dto

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val nickname: String
)
