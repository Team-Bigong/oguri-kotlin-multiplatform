package com.bigong.oguri.dto.request

data class AdminDestinationImageUpsertRequest(
    val imageUrl: String,
    val isThumbnail: Boolean,
    val sortOrder: Int,
)

data class AdminDestinationExperienceUpsertRequest(
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val link: String,
    val sortOrder: Int,
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
    val flightUrl: String? = null,
    val weatherTemp1: Int? = null,
    val weatherPrecipitationMm1: Double? = null,
    val weatherTemp2: Int? = null,
    val weatherPrecipitationMm2: Double? = null,
    val images: List<AdminDestinationImageUpsertRequest>,
    val experiences: List<AdminDestinationExperienceUpsertRequest> = emptyList(),
)
