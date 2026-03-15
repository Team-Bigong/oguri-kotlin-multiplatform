package com.bigong.oguri.dto

import java.time.LocalDate

data class PlaceDetailResponse(
    val id: Long,
    val country: String,
    val city: String,
    val thumbnailUrls: List<String>,
    val isSaved: Boolean,
    val description: String,
    val experiences: List<ExperienceResponse>,
    val flightUrl: String,
    val relevantPlaces: List<PlaceResponse>
)

data class ExperienceResponse(
    val title: String,
    val summary: String,
    val thumbnailUrl: String,
    val advertisementUrl: String
)
