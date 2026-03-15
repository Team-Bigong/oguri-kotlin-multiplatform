package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.PlaceDetail

interface PlaceDetailRepository {
    suspend fun getPlaceDetail(
        placeId: Long,
        startDate: String?,
        endDate: String?,
        userCountry: String,
    ): PlaceDetail

    suspend fun saveDestination(placeId: Long)

    suspend fun deleteSavedDestination(placeId: Long)
}
