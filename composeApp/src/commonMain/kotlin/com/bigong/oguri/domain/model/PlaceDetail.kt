package com.bigong.oguri.domain.model

data class PlaceDetail(
    val id: Long,
    val country: String,
    val city: String,
    val thumbnailUrls: List<String>,
    val isSaved: Boolean,
    val description: String,
    val experiences: List<Experience>,
    val flightUrl: String,
    val relevantPlaces: List<Place>,
)
