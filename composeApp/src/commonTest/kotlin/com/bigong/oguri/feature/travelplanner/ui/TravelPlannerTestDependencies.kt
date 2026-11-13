package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.domain.model.*
import com.bigong.oguri.domain.repository.*
import com.bigong.oguri.domain.usecase.GetPlaceDetailUseCase

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
    )
