package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class CalendarPeriod(
    val id: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
