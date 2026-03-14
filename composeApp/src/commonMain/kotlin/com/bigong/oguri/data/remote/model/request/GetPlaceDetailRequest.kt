package com.bigong.oguri.data.remote.model.request

data class GetPlaceDetailRequest(
    val placeId: Long,
    val includeExperiences: Boolean,
)
