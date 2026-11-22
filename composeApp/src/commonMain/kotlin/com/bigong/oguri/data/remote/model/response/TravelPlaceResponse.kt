package com.bigong.oguri.data.remote.model.response

import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlaceAttribution
import kotlinx.serialization.Serializable

@Serializable
internal data class TravelPlaceResponse(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val attributions: List<TravelPlaceAttributionResponse>,
) {
    fun toDomain(): TravelPlace =
        TravelPlace(
            id = id,
            name = name,
            address = address,
            coordinates = if (latitude != null && longitude != null) TravelCoordinates(latitude, longitude) else null,
            attributions = attributions.map { TravelPlaceAttribution(it.provider, it.providerUri) },
        )
}

@Serializable
internal data class TravelPlaceAttributionResponse(
    val provider: String,
    val providerUri: String?,
)

@Serializable
internal data class TravelDayPlacesResponse(
    val places: List<TravelPlaceResponse>,
    val incomplete: Boolean,
)

@Serializable
internal data class TravelPlanErrorResponse(
    val code: String,
)

@Serializable
internal data class TravelCitySuggestionResponse(
    val id: String,
    val label: String,
    val name: String = label,
    val regionLabel: String? = null,
    val timeZone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    fun toDomain(): TravelCitySuggestion =
        TravelCitySuggestion(
            id, label, name, regionLabel, timeZone,
            if (latitude != null && longitude != null) TravelCoordinates(latitude, longitude) else null,
        )
}

@Serializable
internal data class TravelCityTimeZoneResponse(
    val timeZone: String?,
)
