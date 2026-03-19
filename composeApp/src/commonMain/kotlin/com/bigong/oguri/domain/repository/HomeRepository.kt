package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.domain.model.RecommendationSavedChange
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface HomeRepository {
    suspend fun getRecommendPeriods(userCountry: String): List<RecommendPeriod>
    fun observeRecommendationSavedChanges(): Flow<RecommendationSavedChange>

    suspend fun saveRecommendation(
        startDate: LocalDate,
        endDate: LocalDate,
        dayOffCount: Int,
        totalTripCount: Int,
    )

    suspend fun deleteRecommendation(
        startDate: LocalDate,
        endDate: LocalDate,
        dayOffCount: Int,
        totalTripCount: Int,
    )
}
