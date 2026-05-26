package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate

data class CalendarHoliday(
    val date: LocalDate,
    val name: String,
    val weekend: Boolean,
    val publicHoliday: Boolean,
)
