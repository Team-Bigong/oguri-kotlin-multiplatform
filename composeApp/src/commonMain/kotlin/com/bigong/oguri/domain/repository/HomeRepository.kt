package com.bigong.oguri.domain.repository

import kotlinx.datetime.LocalDate
import com.bigong.oguri.domain.model.RecommendPeriod

interface HomeRepository {
    suspend fun getRecommendPeriods(userCountry: String): List<RecommendPeriod>

    suspend fun saveRecommendation(
        startDate: LocalDate,
        endDate: LocalDate,
        dayOffCount: Int,
    )

    suspend fun deleteRecommendation(
        startDate: LocalDate,
        endDate: LocalDate,
        dayOffCount: Int,
    )
}
