package com.bigong.oguri.domain.model

data class Place(
    val id: Long,
    val country: String,
    val city: String,
    val summary: String,
    val thumbnailUrl: String,
)
