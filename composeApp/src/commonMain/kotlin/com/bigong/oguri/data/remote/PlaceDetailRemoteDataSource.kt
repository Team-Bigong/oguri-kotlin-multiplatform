package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.PlaceDetailResponse

interface PlaceDetailRemoteDataSource {
    suspend fun getPlaceDetailResponse(placeId: Long): PlaceDetailResponse
}
