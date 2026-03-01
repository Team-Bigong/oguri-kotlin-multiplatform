package com.bigong.oguri.data.remote.model.request

data class GetRecommendPeriodsRequest(
    val requestedAtTimestamp: Long,
    val method: String,
)
