package com.bigong.oguri.data.remote.model.request

import kotlinx.serialization.Serializable

@Serializable
data class KakaoLoginRequest(
    val accessToken: String,
)
