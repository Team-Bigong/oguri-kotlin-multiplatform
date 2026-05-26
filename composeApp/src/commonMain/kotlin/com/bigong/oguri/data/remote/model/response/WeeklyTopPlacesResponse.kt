package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeeklyTopPlacesResponse(
    @SerialName("weeklyTopPlaces")
    val weeklyTopPlaces: List<PlaceResponse>,
)
