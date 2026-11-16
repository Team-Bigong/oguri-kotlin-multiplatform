package com.bigong.oguri.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TravelChangeTest {
    private val dates = TravelDates(LocalDate(2027, 4, 1), LocalDate(2027, 4, 3))

    @Test
    fun movedVisitOpensItsCurrentDay() {
        val change = TravelChange(3, 1, "VISIT_EDITED", visitId = "visit", currentDayNumber = 2)
        assertEquals(2, change.destinationDay(dates))
        assertFalse(change.isVisitRemoved)
    }

    @Test
    fun unassignedVisitRemainsNavigable() {
        val change = TravelChange(3, 2, "VISIT_EDITED", visitId = "visit", currentDayNumber = TravelPlan.UNASSIGNED_DAY)
        assertEquals(TravelPlan.UNASSIGNED_DAY, change.destinationDay(dates))
        assertFalse(change.isVisitRemoved)
    }

    @Test
    fun removedVisitDoesNotOpenItsHistoricalDay() {
        val change = TravelChange(3, 1, "VISIT_REMOVED", visitId = "visit")
        assertTrue(change.isVisitRemoved)
        assertNull(change.destinationDay(dates))
    }

    @Test
    fun shortenedTripCannotNavigateOutsideItsCurrentDates() {
        assertNull(TravelChange(3, 6, "DAY_REORDERED").destinationDay(dates))
    }

    @Test
    fun oldServerChangesStillOpenTheirValidDay() {
        val change = TravelChange(3, 1, "VISIT_EDITED")
        assertEquals(1, change.destinationDay(dates))
        assertFalse(change.isVisitRemoved)
    }
}
