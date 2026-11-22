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

    suspend fun searchInCity(planId: String, query: String, language: String, city: String): List<TravelPlace> =
        search(planId, query, language)

    suspend fun search(
        planId: String,
        query: String,
        language: String,
    ): List<TravelPlace>

    suspend fun forPlan(
        planId: String,
        language: String,
        placeIds: List<String>,
    ): TravelDayPlaces

    suspend fun forDay(
        planId: String,
        dayNumber: Int,
        language: String,
    ): TravelDayPlaces
}
