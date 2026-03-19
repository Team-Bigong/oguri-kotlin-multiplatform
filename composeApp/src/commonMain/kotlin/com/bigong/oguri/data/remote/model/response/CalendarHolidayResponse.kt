package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarHolidayResponse(
    @SerialName("date")
    val date: String,
    @SerialName("label")
    val label: String,
    @SerialName("weekend")
    val weekend: Boolean = false,
    @SerialName("publicHoliday")
    val publicHoliday: Boolean = false,
)
