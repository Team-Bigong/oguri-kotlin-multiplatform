package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitDetails
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TravelVisitTimeOrderTest {
    private val morning = visit("morning", 1, "09:00")
    private val untimed = visit("untimed", 1, null)
    private val afternoon = visit("afternoon", 1, "15:00")
    private val visits = listOf(morning, untimed, afternoon)

    @Test
    fun comparesNearestTimedVisitsAcrossUntimedRows() {
        assertTrue(TravelVisitDraft(1, untimed, localTime = "08:00").hasTimeOrderMismatch(visits))
        assertTrue(TravelVisitDraft(1, untimed, localTime = "16:00").hasTimeOrderMismatch(visits))
        assertFalse(TravelVisitDraft(1, untimed, localTime = "12:00").hasTimeOrderMismatch(visits))
        assertFalse(TravelVisitDraft(1, untimed, localTime = "09:00").hasTimeOrderMismatch(visits))
        assertFalse(TravelVisitDraft(1, untimed, localTime = "15:00").hasTimeOrderMismatch(visits))
    }

    @Test
    fun newAndMovedVisitsUseTheEndOfTheTargetDay() {
        assertTrue(TravelVisitDraft(1, localTime = "10:00").hasTimeOrderMismatch(visits))
        val moved = visit("moved", 2, "10:00")
        assertTrue(TravelVisitDraft(1, moved).hasTimeOrderMismatch(visits + moved))
        assertFalse(TravelVisitDraft(1, moved, localTime = "16:00").hasTimeOrderMismatch(visits + moved))
    }

    @Test
    fun ignoresOtherDatesAndTheOriginalTimeOfTheEditedVisit() {
        val elsewhere = visit("elsewhere", 2, "23:00")
        assertFalse(TravelVisitDraft(1, afternoon, localTime = "10:00").hasTimeOrderMismatch(visits + elsewhere))
        assertFalse(TravelVisitDraft(1, morning, localTime = "10:00").hasTimeOrderMismatch(visits))
    }

    @Test
    fun unassignedMissingAndInvalidTimesHaveNoOrderNotice() {
        assertFalse(TravelVisitDraft(0, localTime = "01:00").hasTimeOrderMismatch(visits))
        assertFalse(TravelVisitDraft(1).hasTimeOrderMismatch(visits))
        assertFalse(TravelVisitDraft(1, localTime = "25:00").hasTimeOrderMismatch(visits))
        assertFalse(TravelVisitDraft(1, localTime = "09:").hasTimeOrderMismatch(visits))
        assertFalse(TravelVisitDraft(1, localTime = "08:00").hasTimeOrderMismatch(emptyList()))
    }

    @Test
    fun unrelatedInversionDoesNotWarnOrReorderThePlan() {
        val reversed = listOf(afternoon, morning, untimed)
        val draft = TravelVisitDraft(1, untimed, localTime = "16:00")
        assertFalse(draft.hasTimeOrderMismatch(reversed))
        assertEquals(listOf("afternoon", "morning", "untimed"), reversed.map { it.id })
        assertEquals("16:00", draft.localTime)
    }

    private fun visit(
        id: String,
        day: Int,
        time: String?,
    ): TravelVisit = TravelVisit(id, day, TravelVisitDetails(id, null, time?.let(LocalTime::parse), null), 1)
}
