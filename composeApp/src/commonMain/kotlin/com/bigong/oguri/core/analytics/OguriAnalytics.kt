package com.bigong.oguri.core.analytics

expect fun initializeOguriAnalytics()

expect fun trackOguriEvent(
    eventName: String,
    eventProperties: Map<String, String> = emptyMap(),
)
