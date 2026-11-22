package com.bigong.oguri.core.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSUserDefaults
import kotlin.coroutines.resume

interface TravelReminderPushBridge {
    fun requestPermission(onResult: (Boolean) -> Unit)
    fun currentToken(onResult: (String?) -> Unit)
    fun clearToken(onComplete: () -> Unit)
}

private var pushBridge: TravelReminderPushBridge? = null

fun installTravelReminderPushBridge(bridge: TravelReminderPushBridge) {
    pushBridge = bridge
}

internal actual suspend fun travelReminderPermissionDenied(): Boolean =
    suspendCancellableCoroutine { continuation ->
        platform.UserNotifications.UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            if (continuation.isActive) continuation.resume(settings?.authorizationStatus == platform.UserNotifications.UNAuthorizationStatusDenied)
        }
    }

internal actual fun openTravelReminderSettings() {
    platform.Foundation.NSURL.URLWithString(platform.UIKit.UIApplicationOpenSettingsURLString)?.let { url ->
        platform.UIKit.UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }
}

internal actual suspend fun requestTravelReminderPermission(): Boolean =
    suspendCancellableCoroutine { continuation ->
        val bridge = pushBridge
        if (bridge == null) continuation.resume(false)
        else bridge.requestPermission { if (continuation.isActive) continuation.resume(it) }
    }

internal actual suspend fun travelReminderPushToken(): String? =
    suspendCancellableCoroutine { continuation ->
        val bridge = pushBridge
        if (bridge == null) continuation.resume(null)
        else bridge.currentToken { if (continuation.isActive) continuation.resume(it) }
    }

internal actual suspend fun clearTravelReminderPushToken() {
    suspendCancellableCoroutine { continuation ->
        val bridge = pushBridge
        if (bridge == null) continuation.resume(Unit)
        else bridge.clearToken { if (continuation.isActive) continuation.resume(Unit) }
    }
}

internal actual fun readTravelReminderDeviceState(): String? =
    NSUserDefaults.standardUserDefaults.stringForKey("oguri.travel.reminder.device")

internal actual fun writeTravelReminderDeviceState(value: String) {
    NSUserDefaults.standardUserDefaults.setObject(value, forKey = "oguri.travel.reminder.device")
}
