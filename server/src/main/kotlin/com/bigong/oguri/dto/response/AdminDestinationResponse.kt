package com.bigong.oguri.dto.response

import java.math.BigDecimal

data class AdminDestinationImageResponse(
    val id: Int,
    val imageUrl: String,
    val isThumbnail: Boolean,
    val sortOrder: Int,
)

data class AdminDestinationExperienceResponse(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val link: String,
    val sortOrder: Int,
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
    val flightUrl: String?,
    val weatherTemp1: Int?,
    val weatherPrecipitationMm1: Double?,
    val weatherTemp2: Int?,
    val weatherPrecipitationMm2: Double?,
    val images: List<AdminDestinationImageResponse>,
    val experiences: List<AdminDestinationExperienceResponse>,
)

data class AdminCountryResponse(
    val id: Int,
    val name: String,
    val currencyCode: String?,
    val bigMacIndex: BigDecimal?,
)
