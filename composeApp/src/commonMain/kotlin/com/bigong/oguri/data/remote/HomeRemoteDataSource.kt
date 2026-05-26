package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.request.ManageSavedRecommendationRequest
import com.bigong.oguri.data.remote.model.response.MonthlyTopPeriodResponse
import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse
import com.bigong.oguri.data.remote.model.response.WeeklyTopPlacesResponse

interface HomeRemoteDataSource {
    suspend fun getRecommendPeriodResponses(userCountry: String): List<RecommendPeriodResponse>

    suspend fun getWeeklyTopPlacesResponse(): WeeklyTopPlacesResponse

    suspend fun getMonthlyTopPeriodResponses(): List<MonthlyTopPeriodResponse>

    suspend fun saveRecommendation(request: ManageSavedRecommendationRequest)

    suspend fun deleteRecommendation(request: ManageSavedRecommendationRequest)
}
