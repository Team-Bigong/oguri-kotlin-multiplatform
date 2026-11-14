package com.bigong.oguri.core.analytics

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class TravelUsageSession(
    private val elapsedMillis: () -> Long = TimeSource.Monotonic.markNow().let { origin -> { origin.elapsedNow().inWholeMilliseconds } },
    private val newId: () -> String = { Uuid.random().toString() },
) {
    private val mutex = Mutex()
    private var accountVersion: Long? = null
    private var lastAction: Long? = null
    private var id: String? = null

    // Called only for actual display/user actions and confirmed-command requests, never polling.
    suspend fun current(version: Long): String? =
        mutex.withLock {
            if (accountVersion?.let { version < it } == true) return@withLock null
            val now = elapsedMillis()
            if (accountVersion != version || lastAction == null || now - requireNotNull(lastAction) >= 30 * 60 * 1_000L) {
                id = newId()
            }
            accountVersion = version
            lastAction = now
            requireNotNull(id)
        }
}

internal val travelUsageSession: TravelUsageSession = TravelUsageSession()
