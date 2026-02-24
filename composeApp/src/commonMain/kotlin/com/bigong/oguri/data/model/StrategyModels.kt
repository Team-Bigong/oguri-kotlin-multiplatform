package com.bigong.oguri.data.model

data class HomeStrategyRecommendation(
    val strategyIdentifier: String,
    val startDateText: String,
    val endDateText: String,
    val annualLeaveDaysUsed: Int,
    val totalVacationDaysSecured: Int,
    val topEfficiencyMonths: List<MonthlyStrategyEfficiency>,
    val recommendedDestinations: List<String>,
)

data class MonthlyStrategyEfficiency(
    val monthLabelText: String,
    val securedVacationDays: Int,
)

data class StrategyDetailData(
    val strategyIdentifier: String,
    val titleText: String,
    val annualLeaveUsageDateTexts: List<String>,
    val totalVacationDaysSecured: Int,
    val recommendedDestinations: List<String>,
)

data class StrategyCalendarData(
    val year: Int,
    val monthlyHighlights: List<MonthlyStrategyEfficiency>,
)
