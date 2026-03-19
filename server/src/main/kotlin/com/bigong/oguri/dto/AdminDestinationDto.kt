package com.bigong.oguri.dto

data class AdminDestinationImageUpsertRequest(
    val imageUrl: String,
    val isThumbnail: Boolean,
    val sortOrder: Int
)

data class AdminDestinationUpsertRequest(
    val countryId: Int,
    val name: String,
    val summary: String?,
    val description: String?,
    val recommendStartMonth1: Int?,
    val recommendEndMonth1: Int?,
    val recommendStartMonth2: Int?,
    val recommendEndMonth2: Int?,
    val flightTime: String?,
    val images: List<AdminDestinationImageUpsertRequest>
)

data class AdminDestinationImageResponse(
    val id: Int,
    val imageUrl: String,
    val isThumbnail: Boolean,
    val sortOrder: Int
)

data class AdminDestinationResponse(
    val id: Int,
    val countryId: Int?,
    val countryName: String,
    val name: String,
    val summary: String?,
    val description: String?,
    val recommendStartMonth1: Int?,
    val recommendEndMonth1: Int?,
    val recommendStartMonth2: Int?,
    val recommendEndMonth2: Int?,
    val flightTime: String?,
    val images: List<AdminDestinationImageResponse>
)

data class AdminCountryResponse(
    val id: Int,
    val name: String
)
