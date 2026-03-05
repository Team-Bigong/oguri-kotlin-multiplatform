package com.bigong.oguri.data.remote.model.response

import kotlinx.serialization.Serializable

@Serializable
data class CalendarPeriodResponse(
    val id: Long,
    val startDate: String,
    val endDate: String,
)
