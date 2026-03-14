package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ExperienceResponse(
    val title: String,
    val summary: String,
    val thumbnailUrl: String,
    val advertisementUrl: String,
)
