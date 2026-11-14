package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class TravelRecoveryTest {
    @Test
    fun confirmedPlanKeepsItsModificationTimeAndAcceptsOlderCacheEntries() {
        val plan =
            TravelPlan(
                TravelPlanSummary(
                    "plan",
                    TravelPlanDetails(
                        "Seoul",
                        TimeZone.of("Asia/Seoul"),
                        "Trip",
                        TravelDates(LocalDate(2027, 4, 1), LocalDate(2027, 4, 3)),
                    ),
                ),
                revision = 1,
                dayOrderRevisions = emptyMap(),
                visits = emptyList(),
                lastModifiedAt = Instant.parse("2027-03-31T10:00:00Z"),
            )
        assertEquals(plan, Json.decodeFromString<TravelPlan>(Json.encodeToString(plan)))
        val olderCache = Json.encodeToString(plan.copy(lastModifiedAt = null))
        assertNull(Json.decodeFromString<TravelPlan>(olderCache).lastModifiedAt)
    }

    @Test
    fun pendingEditKeepsTheOriginalRequestAndConflictBase() {
        val original =
            TravelVisit(
                id = "visit",
                dayNumber = 1,
                details = TravelVisitDetails(null, null, LocalTime(9, 30), "Original note", "place"),
                revision = 8,
            )
        val request: TravelPendingSave =
            TravelPendingSave.Visit(
                requestId = "edit-request",
                change = TravelVisitChange.Edit(original, 2, original.details.copy(note = "New note"), structureRevision = 3),
                selectedDay = 2,
            )

        val restored = Json.decodeFromString<TravelPendingSave>(Json.encodeToString(request))

        assertEquals(request, restored)
    }

    @Test
    fun pendingUndoKeepsBothRequestIdentifiers() {
        val request: TravelPendingSave =
            TravelPendingSave.Visit(
                requestId = "undo-request",
                change = TravelVisitChange.Undo(originalRequestId = "delete-request"),
                selectedDay = 1,
            )

        val restored = Json.decodeFromString<TravelPendingSave>(Json.encodeToString(request))

        assertEquals(request, restored)
    }
}
