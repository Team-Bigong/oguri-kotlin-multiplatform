package com.bigong.oguri.core.analytics

import com.bigong.oguri.BuildConfig
import com.amplitude.android.Amplitude
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.Configuration
import com.amplitude.android.plugins.SessionReplayPlugin
import com.bigong.oguri.core.network.AMPLITUDE_API_KEY
import com.bigong.oguri.core.platform.OguriPlatformContextHolder

private object AndroidOguriAnalyticsManager {
    private var amplitude: Amplitude? = null

    fun initialize() {
        if (BuildConfig.DEBUG) {
            return
        }
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
        if (BuildConfig.DEBUG) {
            return
        }
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
