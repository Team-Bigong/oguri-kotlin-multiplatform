package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PlaceResponse(
    val id: Long,
    val country: String,
    val city: String,
    val summary: String,
    val thumbnailUrl: String,
)
