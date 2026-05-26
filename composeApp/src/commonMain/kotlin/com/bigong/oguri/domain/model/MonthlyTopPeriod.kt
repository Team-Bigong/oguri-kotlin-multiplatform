package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class MonthlyTopPeriod(
    val rank: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalTripCount: Int,
    val holidayCount: Int,
    val dayOffCount: Int,
)
