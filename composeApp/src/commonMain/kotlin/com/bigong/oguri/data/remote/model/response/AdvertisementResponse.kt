package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AdvertisementResponse(
    val platform: String,
    val url: String,
)
