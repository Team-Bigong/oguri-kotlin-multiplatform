package com.bigong.oguri.data.remote.model.request

import kotlinx.serialization.Serializable

@Serializable
data class GetCalendarRecommendationRequest(
    val leaveDays: Int,
    val year: Int,
    val month: Int,
)
