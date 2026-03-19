package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("country")
    val country: String,
    @SerialName("city")
    val city: String,
    @SerialName("summary")
    val summary: String = "",
    @SerialName("thumbnailUrl")
    val thumbnailUrl: String,
    @SerialName("saved")
    val saved: Boolean = false,
)
