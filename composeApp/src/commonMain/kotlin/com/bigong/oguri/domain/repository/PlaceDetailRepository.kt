package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.PlaceDetail

interface PlaceDetailRepository {
    suspend fun getPlaceDetail(placeId: Long): PlaceDetail
}
