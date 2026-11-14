package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelDraft
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitDetails
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TravelConflictComparisonTest {
    private val original = TravelVisit("visit", 1, TravelVisitDetails("Museum", "Address", null, "Original"), 1)

    @Test
    fun regularEditingDoesNotShowAComparison() {
        val draft = TravelVisitDraft(1, original, note = "My input")
        assertTrue(draft.latestDifferences().isEmpty())
    }

    @Test
    fun explicitReloadKeepsMyInputSeparateFromTheLatestSharedMemo() {
        val draft = TravelVisitDraft(1, original, note = "My input")
        val latest = original.copy(revision = 2, details = original.details.copy(note = "Other member's note"))
        val rebased = draft.rebaseOn(latest)
        assertEquals("My input", rebased.note)
        assertEquals("Other member's note", rebased.visit?.details?.note)
        assertEquals(setOf(TravelVisitField.NOTE), rebased.latestDifferences())
    }

    @Test
    fun clearedAndIndependentlyUpdatedFieldsAreComparedAccurately() {
        val draft = TravelVisitDraft(1, original, location = "My address", note = "My input")
        val latest = original.copy(revision = 2, details = original.details.copy(location = null, localTime = LocalTime(10, 30)))
        val rebased = draft.rebaseOn(latest)
        assertEquals("10:30", rebased.localTime)
        assertEquals(setOf(TravelVisitField.LOCATION, TravelVisitField.NOTE), rebased.latestDifferences())
    }

    @Test
    fun comparisonAndBothValuesSurviveDraftRecovery() {
        val latest = original.copy(revision = 2, details = original.details.copy(note = "Shared update"))
        val rebased = TravelVisitDraft(1, original, note = "My input").rebaseOn(latest)
        val restored = Json.decodeFromString<TravelDraft>(Json.encodeToString(rebased.forRecovery())).toVisitDraft()
        assertTrue(restored.showLatestComparison)
        assertEquals("My input", restored.note)
        assertEquals("Shared update", restored.visit?.details?.note)
        assertEquals(setOf(TravelVisitField.NOTE), restored.latestDifferences())
        val legacy = rebased.forRecovery().copy(showLatestComparison = false)
        assertFalse(Json.decodeFromString<TravelDraft>(Json.encodeToString(legacy)).showLatestComparison)
    }
}
