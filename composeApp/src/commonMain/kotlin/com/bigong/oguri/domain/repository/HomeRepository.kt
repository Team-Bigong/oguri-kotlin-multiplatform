package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.MonthlyTopPeriod
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.domain.model.RecommendationSavedChange
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface HomeRepository {
    suspend fun getRecommendPeriods(userCountry: String): List<RecommendPeriod>

    suspend fun getWeeklyTopPlaces(): List<Place>

    suspend fun getMonthlyTopPeriods(): List<MonthlyTopPeriod>

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
