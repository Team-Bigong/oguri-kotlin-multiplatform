package com.bigong.oguri.data.remote

import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.data.model.StrategyCalendarData
import com.bigong.oguri.data.model.StrategyDetailData

interface StrategyRemoteDataSource {
    suspend fun getHomeStrategyRecommendation(): HomeStrategyRecommendation

    suspend fun getStrategyDetail(strategyIdentifier: String): StrategyDetailData

    suspend fun getStrategyCalendar(year: Int): StrategyCalendarData
}
