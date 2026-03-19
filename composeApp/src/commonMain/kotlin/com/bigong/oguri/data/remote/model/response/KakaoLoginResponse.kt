package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class KakaoLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val nickname: String,
)
