package com.bigong.oguri.domain.repository

import com.bigong.oguri.domain.model.RecommendPeriod
import kotlinx.datetime.LocalDate

interface HomeRepository {
    suspend fun getRecommendPeriods(userCountry: String): List<RecommendPeriod>

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
