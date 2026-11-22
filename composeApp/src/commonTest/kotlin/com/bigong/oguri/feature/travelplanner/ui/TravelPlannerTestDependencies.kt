package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.domain.model.*
import com.bigong.oguri.domain.repository.*
import com.bigong.oguri.domain.usecase.GetPlaceDetailUseCase
import com.bigong.oguri.domain.usecase.GetSearchAutocompleteListUseCase

internal fun createTravelPlannerTestViewModel(
    plans: TravelPlanRepository,
    recovery: TravelRecoveryRepository,
    placeRepository: TravelPlaceRepository? = null,
): TravelPlannerViewModel =
    TravelPlannerViewModel(
        plans,
        placeRepository ?: object : TravelPlaceRepository {
            override suspend fun cities(
                query: String,
                language: String,
            ): List<TravelCitySuggestion> = error("Unused")

            override suspend fun cityTimeZone(placeId: String): String? = error("Unused")

            override suspend fun search(
                planId: String,
                query: String,
                language: String,
            ): List<TravelPlace> = error("Unused")

            override suspend fun forPlan(
                planId: String,
                language: String,
                placeIds: List<String>,
            ): TravelDayPlaces = error("Unused")

            override suspend fun forDay(
                planId: String,
                dayNumber: Int,
                language: String,
            ): TravelDayPlaces = error("Unused")
        },
        GetPlaceDetailUseCase(
            object : PlaceDetailRepository {
                override suspend fun getPlaceDetail(
                    placeId: Long,
                    startDate: String?,
                    endDate: String?,
                    userCountry: String,
                ): PlaceDetail = error("Unused")

                override suspend fun saveDestination(placeId: Long): Unit = error("Unused")

                override suspend fun deleteSavedDestination(placeId: Long): Unit = error("Unused")
            },
        ),
        recovery,
        GetSearchAutocompleteListUseCase(
            object : SearchRepository {
                override suspend fun getSearchAutocompletes(query: String): List<SearchAutocomplete> = error("Unused")
                override fun getRecentSearchKeywords(): List<String> = error("Unused")
                override fun addRecentSearchKeyword(keyword: String): List<String> = error("Unused")
                override fun deleteRecentSearchKeyword(keyword: String): List<String> = error("Unused")
            },
        ),
    )
