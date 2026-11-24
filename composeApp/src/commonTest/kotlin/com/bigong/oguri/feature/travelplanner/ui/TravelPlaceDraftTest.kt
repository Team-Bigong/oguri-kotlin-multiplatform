package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelVisitCategory
import com.bigong.oguri.feature.travelplanner.ui.component.hasUnsavedPlaceInput
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlaceSearchState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TravelPlaceDraftTest {
    @Test
    fun googlePlaceSelectionRequiresDiscardConfirmationWithoutAManualDraft() {
        val place = TravelPlace("place", "Market", "Address", TravelCoordinates(1.0, 2.0), emptyList())
        assertTrue(hasUnsavedPlaceInput(null, place, ""))
    }

    @Test
    fun eachResultStartsWithItsSuggestionOrOtherAndReselectingKeepsCategoryEdits() {
        val market = TravelPlace("market", "Market", null, null, emptyList(), suggestedCategory = TravelVisitCategory.SHOPPING)
        val unclassified = TravelPlace("unknown", "Unknown", null, null, emptyList())
        val search = TravelPlaceSearchState(results = listOf(market, unclassified))

        val selected = search.select(market.id)
        assertEquals(TravelVisitCategory.SHOPPING, selected.selection?.category)

        val edited = selected.selectCategory(TravelVisitCategory.CAFE).select(market.id)
        assertEquals(TravelVisitCategory.CAFE, edited.selection?.category)

        val cleared = edited.selectCategory(null).select(market.id)
        assertNull(cleared.selection?.category)
        assertEquals(TravelVisitCategory.OTHER, cleared.select(unclassified.id).selection?.category)
    }

    @Test
    fun searchNotesRequireDiscardConfirmationEvenAfterTheSelectionIsCleared() {
        assertTrue(hasUnsavedPlaceInput(null, null, "Meet at the entrance"))
        assertFalse(hasUnsavedPlaceInput(null, null, "   "))
    }

    @Test
    fun emptyFormsCanCloseButManualNotesArePreserved() {
        val draft = TravelVisitDraft(dayNumber = 2)
        assertFalse(hasUnsavedPlaceInput(null, null, ""))
        assertFalse(hasUnsavedPlaceInput(draft, null, ""))
        assertTrue(hasUnsavedPlaceInput(draft.copy(note = "Meet at the entrance"), null, ""))
    }
}
