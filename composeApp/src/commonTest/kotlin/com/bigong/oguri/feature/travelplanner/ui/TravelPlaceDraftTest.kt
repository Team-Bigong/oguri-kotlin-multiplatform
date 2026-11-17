package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelVisitCategory
import com.bigong.oguri.feature.travelplanner.ui.component.hasUnsavedPlaceInput
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TravelPlaceDraftTest {
    @Test
    fun googlePlaceSelectionAndCategoryRequireDiscardConfirmationWithoutAManualDraft() {
        val place = TravelPlace("place", "Market", "Address", TravelCoordinates(1.0, 2.0), emptyList())
        assertTrue(hasUnsavedPlaceInput(null, place, null, ""))
        assertTrue(hasUnsavedPlaceInput(null, null, TravelVisitCategory.SHOPPING, ""))
    }

    @Test
    fun searchNotesRequireDiscardConfirmationEvenAfterTheSelectionIsCleared() {
        assertTrue(hasUnsavedPlaceInput(null, null, null, "Meet at the entrance"))
        assertFalse(hasUnsavedPlaceInput(null, null, null, "   "))
    }

    @Test
    fun emptyFormsCanCloseButManualNotesArePreserved() {
        val draft = TravelVisitDraft(dayNumber = 2)
        assertFalse(hasUnsavedPlaceInput(null, null, null, ""))
        assertFalse(hasUnsavedPlaceInput(draft, null, null, ""))
        assertTrue(hasUnsavedPlaceInput(draft.copy(note = "Meet at the entrance"), null, null, ""))
    }
}
