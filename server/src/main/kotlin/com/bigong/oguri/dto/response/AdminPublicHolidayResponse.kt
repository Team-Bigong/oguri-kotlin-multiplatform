package com.bigong.oguri.dto.response

import java.time.LocalDate

data class AdminPublicHolidayResponse(
    val id: Int,
    val holidayDate: LocalDate,
    val name: String,
    val isActualHoliday: Boolean,
)
