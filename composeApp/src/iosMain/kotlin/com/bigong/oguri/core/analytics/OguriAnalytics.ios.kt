package com.bigong.oguri.core.analytics

import com.bigong.oguri.core.network.AMPLITUDE_API_KEY
import platform.Foundation.NSNotificationCenter

private const val IOS_NOTIFICATION_AMPLITUDE_INITIALIZE = "OguriAmplitudeInitialize"
private const val IOS_NOTIFICATION_AMPLITUDE_TRACK = "OguriAmplitudeTrack"
private const val IOS_NOTIFICATION_KEY_API_KEY = "apiKey"
private const val IOS_NOTIFICATION_KEY_EVENT_NAME = "eventName"
private const val IOS_NOTIFICATION_KEY_EVENT_PROPERTIES = "eventProperties"

actual fun initializeOguriAnalytics() {
    if (AMPLITUDE_API_KEY.isBlank()) {
        return
    }
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = IOS_NOTIFICATION_AMPLITUDE_INITIALIZE,
        `object` = null,
        userInfo = mapOf(IOS_NOTIFICATION_KEY_API_KEY to AMPLITUDE_API_KEY),
    )
}

actual fun trackOguriEvent(
    eventName: String,
    eventProperties: Map<String, String>,
) {
    if (eventName.isBlank()) {
        return
    }
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = IOS_NOTIFICATION_AMPLITUDE_TRACK,
        `object` = null,
        userInfo =
            mapOf(
                IOS_NOTIFICATION_KEY_EVENT_NAME to eventName,
                IOS_NOTIFICATION_KEY_EVENT_PROPERTIES to eventProperties,
            ),
    )
}
