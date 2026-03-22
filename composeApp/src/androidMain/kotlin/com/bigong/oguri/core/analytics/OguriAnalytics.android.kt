package com.bigong.oguri.core.analytics

import android.content.pm.ApplicationInfo
import com.amplitude.android.Amplitude
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.Configuration
import com.amplitude.android.plugins.SessionReplayPlugin
import com.bigong.oguri.core.network.AMPLITUDE_API_KEY
import com.bigong.oguri.core.platform.OguriPlatformContextHolder

private object AndroidOguriAnalyticsManager {
    private var amplitude: Amplitude? = null

    fun initialize() {
        if (isDebuggableApp()) return
        if (amplitude != null || AMPLITUDE_API_KEY.isBlank()) {
            return
        }
        val applicationContext = OguriPlatformContextHolder.applicationContext ?: return
        amplitude =
            Amplitude(
                configuration =
                    Configuration(
                        apiKey = AMPLITUDE_API_KEY,
                        context = applicationContext,
                        autocapture = AutocaptureOption.ALL,
                    ),
            ).also { initializedAmplitude ->
                initializedAmplitude.add(SessionReplayPlugin())
            }
    }

    fun track(
        eventName: String,
        eventProperties: Map<String, String>,
    ) {
        if (isDebuggableApp()) return
        if (eventName.isBlank()) {
            return
        }
        initialize()
        val activeAmplitude: Amplitude = amplitude ?: return
        if (eventProperties.isEmpty()) {
            activeAmplitude.track(eventName)
            return
        }
        activeAmplitude.track(eventName, eventProperties)
    }

    private fun isDebuggableApp(): Boolean {
        val applicationContext = OguriPlatformContextHolder.applicationContext ?: return false
        return (applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}

actual fun initializeOguriAnalytics() {
    AndroidOguriAnalyticsManager.initialize()
}

actual fun trackOguriEvent(
    eventName: String,
    eventProperties: Map<String, String>,
) {
    AndroidOguriAnalyticsManager.track(
        eventName = eventName,
        eventProperties = eventProperties,
    )
}
