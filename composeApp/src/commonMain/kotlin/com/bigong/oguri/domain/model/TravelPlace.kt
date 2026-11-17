package com.bigong.oguri.domain.model

import kotlinx.serialization.Serializable

@Serializable
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

@Serializable
data class TravelCitySuggestion(
    val id: String,
    val label: String,
    val name: String = label,
    val regionLabel: String? = null,
    val timeZone: String? = null,
    val coordinates: TravelCoordinates? = null,
) {
    val contextualName: String get() = regionLabel?.let { "$name · $it" } ?: name
}
