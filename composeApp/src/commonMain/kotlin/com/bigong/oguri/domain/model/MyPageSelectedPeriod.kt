package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class MyPageSelectedPeriod(
    val id: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalTripCount: Int,
    val dayOffCount: Int,
)
