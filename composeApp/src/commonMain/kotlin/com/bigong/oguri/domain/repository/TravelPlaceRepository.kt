package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelDayPlaces
import com.bigong.oguri.domain.model.TravelPlace

interface TravelPlaceRepository {
    suspend fun cities(
        query: String,
        language: String,
    ): List<TravelCitySuggestion>

    suspend fun cityTimeZone(placeId: String): String?

    suspend fun search(
        planId: String,
        query: String,
        language: String,
    ): List<TravelPlace>

    suspend fun forDay(
        planId: String,
        dayNumber: Int,
        language: String,
    ): TravelDayPlaces
}
