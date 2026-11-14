package com.bigong.oguri.domain.model

data class TravelCoordinates(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0 && longitude in -180.0..180.0)
    }
}

data class TravelPlace(
    val id: String,
    val name: String,
    val address: String?,
    val coordinates: TravelCoordinates?,
    val attributions: List<TravelPlaceAttribution>,
)

data class TravelPlaceAttribution(
    val provider: String,
    val providerUri: String?,
)

data class TravelDayPlaces(
    val places: Map<String, TravelPlace>,
    val incomplete: Boolean,
)

data class TravelCitySuggestion(
    val id: String,
    val label: String,
)
