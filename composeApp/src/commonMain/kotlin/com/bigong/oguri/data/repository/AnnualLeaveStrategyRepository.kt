package com.bigong.oguri.data.repository

import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.data.model.StrategyCalendarData
import com.bigong.oguri.data.model.StrategyDetailData

interface AnnualLeaveStrategyRepository {
    suspend fun getHomeStrategyRecommendation(): HomeStrategyRecommendation

    suspend fun getStrategyDetail(
        strategyIdentifier: String,
    ): StrategyDetailData

    suspend fun getStrategyCalendar(
        year: Int,
    ): StrategyCalendarData
}
