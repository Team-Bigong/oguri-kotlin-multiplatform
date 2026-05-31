package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDetailResponse(
    val id: Long,
    val country: String,
    val city: String,
    val thumbnailUrls: List<String>,
    val exchangeRateInfo: PlaceDetailExchangeRateInfoResponse?,
    val relativeCostIndex: Double?,
    val averageTemperature: Int?,
    val averagePrecipitation: Double?,
    @SerialName("saved")
    val isSaved: Boolean,
    val description: String,
    val recommendPeriod: PlaceDetailRecommendPeriodResponse?,
    val experiences: List<ExperienceResponse>,
    val flightUrl: String,
    val relevantPlaces: List<PlaceResponse>,
)

@Serializable
data class PlaceDetailExchangeRateInfoResponse(
    val krwAmount: Int,
    val currencyUnit: Int,
    val currencyCode: String,
    val date: String,
)

@Serializable
data class PlaceDetailRecommendPeriodResponse(
    val startMonth: Int,
    val endMonth: Int,
)
