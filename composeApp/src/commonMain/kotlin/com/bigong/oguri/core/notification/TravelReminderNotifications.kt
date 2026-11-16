package com.bigong.oguri.core.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val mutableReminderOpening: MutableStateFlow<String?> = MutableStateFlow(null)
internal val travelReminderOpening: StateFlow<String?> = mutableReminderOpening.asStateFlow()

@OptIn(ExperimentalUuidApi::class)
fun handleTravelReminderNotification(reminderId: String) {
    if (runCatching { Uuid.parse(reminderId) }.isSuccess) mutableReminderOpening.value = reminderId
}

internal fun consumeTravelReminderOpening(reminderId: String) {
    mutableReminderOpening.compareAndSet(reminderId, null)
}

private val mutableTokenVersion: MutableStateFlow<Int> = MutableStateFlow(0)
internal val travelReminderTokenVersion: StateFlow<Int> = mutableTokenVersion.asStateFlow()

fun onTravelReminderTokenChanged() {
    mutableTokenVersion.update { it + 1 }
}

internal expect suspend fun travelReminderPermissionDenied(): Boolean

internal expect fun openTravelReminderSettings()

internal expect suspend fun requestTravelReminderPermission(): Boolean

internal expect suspend fun travelReminderPushToken(): String?

internal expect suspend fun clearTravelReminderPushToken()

internal expect fun readTravelReminderDeviceState(): String?

internal expect fun writeTravelReminderDeviceState(value: String)
