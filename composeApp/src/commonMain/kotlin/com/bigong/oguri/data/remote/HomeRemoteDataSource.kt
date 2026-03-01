package com.bigong.oguri.data.remote

import com.bigong.oguri.data.remote.model.response.RecommendPeriodResponse

interface HomeRemoteDataSource {
    suspend fun getRecommendPeriodResponses(): List<RecommendPeriodResponse>
}
