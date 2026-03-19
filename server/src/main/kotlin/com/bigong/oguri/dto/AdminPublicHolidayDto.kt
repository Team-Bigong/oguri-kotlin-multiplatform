package com.bigong.oguri.dto

import java.time.LocalDate

data class AdminPublicHolidayUpsertRequest(
    val holidayDate: LocalDate,
    val name: String,
    val isActualHoliday: Boolean
)

data class AdminPublicHolidayResponse(
    val id: Int,
    val holidayDate: LocalDate,
    val name: String,
    val isActualHoliday: Boolean
)
