package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.request.ManageSavedRecommendationRequest
import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse

interface HomeRemoteDataSource {
    suspend fun getRecommendPeriodResponses(userCountry: String): List<RecommendPeriodResponse>

    suspend fun saveRecommendation(request: ManageSavedRecommendationRequest)

    suspend fun deleteRecommendation(request: ManageSavedRecommendationRequest)
}
