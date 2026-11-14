package com.bigong.oguri.core.analytics

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class TravelUsageSessionTest {
    @Test
    fun actionsWithinThirtyMinutesKeepTheSessionAndExtendItsIdleBoundary() =
        runTest {
            var now = 0L
            var sequence = 0
            val session = TravelUsageSession({ now }, { "session-${++sequence}" })
            val first = session.current(1)
            repeat(3) {
                now += 20 * 60 * 1_000L
                assertEquals(first, session.current(1))
            }
            now += 30 * 60 * 1_000L
            assertNotEquals(first, session.current(1))
        }

    @Test
    fun accountChangesAndNewProcessesStartDifferentSessions() =
        runTest {
            var sequence = 0
            val newId = { "session-${++sequence}" }
            val session = TravelUsageSession({ 0 }, newId)
            val first = session.current(1)
            val switched = session.current(2)
            assertNotEquals(first, switched)
            assertNull(session.current(1))
            assertEquals(switched, session.current(2))
            assertNotEquals(switched, session.current(3))
            assertNotEquals(first, TravelUsageSession({ 0 }, newId).current(1))
        }

    @Test
    fun simultaneousActionsDoNotSplitOneSession() =
        runTest {
            var sequence = 0
            val session = TravelUsageSession({ 0 }, { "session-${++sequence}" })
            val ids = List(20) { async { session.current(1) } }.awaitAll()
            assertEquals(1, ids.toSet().size)
            assertEquals(1, sequence)
        }
}
