package com.bigong.oguri.domain.model

data class PlaceDetail(
    val id: Long,
    val country: String,
    val city: String,
    val thumbnailUrls: List<String>,
    val isSaved: Boolean,
    val travelInformation: PlaceDetailTravelInformation,
    val description: String,
    val experiences: List<Experience>,
    val flightUrl: String,
    val relevantPlaces: List<Place>,
)

data class PlaceDetailTravelInformation(
    val exchangeRateInformation: PlaceDetailExchangeRateInformation,
    val relativeCostIndex: Double,
    val averageTemperature: Int,
    val averagePrecipitation: Double,
    val recommendPeriod: PlaceDetailRecommendPeriod,
)

data class PlaceDetailExchangeRateInformation(
    val koreanWonAmount: Int,
    val currencyUnit: Int,
    val currencyCode: String,
    val date: String,
)

data class PlaceDetailRecommendPeriod(
    val startMonth: Int,
    val endMonth: Int,
)
