package com.bigong.oguri.dto.response

import com.bigong.oguri.domain.TravelVisitCategory
import com.fasterxml.jackson.annotation.JsonInclude

data class TravelPlaceResponse(
    val id: String,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val attributions: List<TravelPlaceAttribution>,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    val suggestedCategory: TravelVisitCategory? = null,
)

data class TravelPlaceAttribution(
    val provider: String,
    val providerUri: String?,
)

data class TravelDayPlacesResponse(
    val places: List<TravelPlaceResponse>,
    val incomplete: Boolean,
)

data class TravelCitySuggestion(
    val id: String,
    val label: String,
    val name: String,
    val regionLabel: String,
    val timeZone: String,
    val latitude: Double,
    val longitude: Double,
)

data class TravelCityTimeZoneResponse(
    val timeZone: String?,
)
