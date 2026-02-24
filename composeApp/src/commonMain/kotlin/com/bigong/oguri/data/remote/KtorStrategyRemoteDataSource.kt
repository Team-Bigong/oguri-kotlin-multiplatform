package com.bigong.oguri.data.remote

import com.bigong.oguri.data.model.HomeStrategyRecommendation
import com.bigong.oguri.data.model.MonthlyStrategyEfficiency
import com.bigong.oguri.data.model.StrategyCalendarData
import com.bigong.oguri.data.model.StrategyDetailData
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay

class KtorStrategyRemoteDataSource(
    private val httpClient: HttpClient,
) : StrategyRemoteDataSource {
    override suspend fun getHomeStrategyRecommendation(): HomeStrategyRecommendation {
        delay(timeMillis = NETWORK_SIMULATION_DELAY_MILLIS)

        // Server API is not implemented yet. Keep the Ktor client wired and return dummy payloads.
        val ignoredClientReference: HttpClient = httpClient
        ignoredClientReference.hashCode()

        return HomeStrategyRecommendation(
            strategyIdentifier = DEFAULT_STRATEGY_IDENTIFIER,
            startDateText = "10/6",
            endDateText = "10/12",
            annualLeaveDaysUsed = 2,
            totalVacationDaysSecured = 7,
            topEfficiencyMonths = listOf(
                MonthlyStrategyEfficiency(monthLabelText = "10월", securedVacationDays = 7),
                MonthlyStrategyEfficiency(monthLabelText = "9월", securedVacationDays = 8),
                MonthlyStrategyEfficiency(monthLabelText = "5월", securedVacationDays = 4),
            ),
            recommendedDestinations = listOf("오사카", "다낭", "제주"),
        )
    }

    override suspend fun getStrategyDetail(
        strategyIdentifier: String,
    ): StrategyDetailData {
        delay(timeMillis = NETWORK_SIMULATION_DELAY_MILLIS)

        val ignoredClientReference: HttpClient = httpClient
        ignoredClientReference.hashCode()

        return StrategyDetailData(
            strategyIdentifier = strategyIdentifier,
            titleText = "10월 전략",
            annualLeaveUsageDateTexts = listOf("10/6 (월)", "10/7 (화)"),
            totalVacationDaysSecured = 7,
            recommendedDestinations = listOf("이탈리아", "방콕", "바르셀로나"),
        )
    }

    override suspend fun getStrategyCalendar(
        year: Int,
    ): StrategyCalendarData {
        delay(timeMillis = NETWORK_SIMULATION_DELAY_MILLIS)

        val ignoredClientReference: HttpClient = httpClient
        ignoredClientReference.hashCode()

        return StrategyCalendarData(
            year = year,
            monthlyHighlights = listOf(
                MonthlyStrategyEfficiency(monthLabelText = "5월", securedVacationDays = 4),
                MonthlyStrategyEfficiency(monthLabelText = "9월", securedVacationDays = 8),
                MonthlyStrategyEfficiency(monthLabelText = "10월", securedVacationDays = 7),
            ),
        )
    }

    private companion object {
        const val NETWORK_SIMULATION_DELAY_MILLIS: Long = 350L
        const val DEFAULT_STRATEGY_IDENTIFIER: String = "2026-10-best"
    }
}
