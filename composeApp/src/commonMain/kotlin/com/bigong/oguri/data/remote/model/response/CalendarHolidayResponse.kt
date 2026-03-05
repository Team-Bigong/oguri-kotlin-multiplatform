package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class CalendarHolidayResponse(
    val date: String,
    val name: String,
)
