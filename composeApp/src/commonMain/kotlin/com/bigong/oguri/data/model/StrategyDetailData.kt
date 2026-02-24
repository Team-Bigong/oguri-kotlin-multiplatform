package com.bigong.oguri.data.model

data class StrategyDetailData(
    val strategyIdentifier: String,
    val titleText: String,
    val annualLeaveUsageDateTexts: List<String>,
    val totalVacationDaysSecured: Int,
    val recommendedDestinations: List<String>,
)
