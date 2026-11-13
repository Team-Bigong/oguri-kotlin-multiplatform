package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TravelRecoveryTest {
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
