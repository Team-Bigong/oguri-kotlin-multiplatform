package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PlaceDetailResponse(
    val id: Long,
    val country: String,
    val city: String,
    val thumbnailUrls: List<String>,
    val isSaved: Boolean,
    val description: String,
    val experiences: List<ExperienceResponse>,
    val flightUrl: String,
    val relevantPlaces: List<PlaceResponse>,
)
