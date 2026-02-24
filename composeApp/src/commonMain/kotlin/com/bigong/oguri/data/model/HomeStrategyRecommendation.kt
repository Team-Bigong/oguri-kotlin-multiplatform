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
