package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.RecommendPeriod

interface HomeRepository {
    suspend fun getRecommendPeriods(): List<RecommendPeriod>
}
