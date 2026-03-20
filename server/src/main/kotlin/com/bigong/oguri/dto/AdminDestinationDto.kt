package com.bigong.oguri.dto

data class AdminDestinationImageUpsertRequest(
    val imageUrl: String,
    val isThumbnail: Boolean,
    val sortOrder: Int
)

data class AdminDestinationExperienceUpsertRequest(
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val link: String,
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
    val flightTimeMinutes: Int?,
    val images: List<AdminDestinationImageUpsertRequest>,
    val experiences: List<AdminDestinationExperienceUpsertRequest> = emptyList()
)

data class AdminDestinationImageResponse(
    val id: Int,
    val imageUrl: String,
    val isThumbnail: Boolean,
    val sortOrder: Int
)

data class AdminDestinationExperienceResponse(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val link: String,
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
    val flightTimeMinutes: Int?,
    val images: List<AdminDestinationImageResponse>,
    val experiences: List<AdminDestinationExperienceResponse>
)

data class AdminCountryResponse(
    val id: Int,
    val name: String
)
