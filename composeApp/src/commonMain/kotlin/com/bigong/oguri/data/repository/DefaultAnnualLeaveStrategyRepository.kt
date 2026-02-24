package com.bigong.oguri.data.repository

import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.data.model.StrategyCalendarData
import com.bigong.oguri.data.model.StrategyDetailData
import com.bigong.oguri.data.remote.StrategyRemoteDataSource

class DefaultAnnualLeaveStrategyRepository(
    private val strategyRemoteDataSource: StrategyRemoteDataSource,
) : AnnualLeaveStrategyRepository {
    override suspend fun getHomeStrategyRecommendation(): HomeStrategyRecommendation {
        return strategyRemoteDataSource.getHomeStrategyRecommendation()
    }

    override suspend fun getStrategyDetail(
        strategyIdentifier: String,
    ): StrategyDetailData {
        return strategyRemoteDataSource.getStrategyDetail(strategyIdentifier = strategyIdentifier)
    }

    override suspend fun getStrategyCalendar(
        year: Int,
    ): StrategyCalendarData {
        return strategyRemoteDataSource.getStrategyCalendar(year = year)
    }
}
