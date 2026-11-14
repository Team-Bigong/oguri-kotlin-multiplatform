package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitChange
import com.bigong.oguri.domain.model.TravelVisitDetails
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TravelPlanRefreshTest {
    private val dates = TravelDates(LocalDate(2027, 4, 1), LocalDate(2027, 4, 3))
    private val visit = TravelVisit("visit", 3, TravelVisitDetails("Museum", null, null, "Shared note"), 1)
    private val plan =
        TravelPlan(
            TravelPlanSummary("plan", TravelPlanDetails("Seoul", TimeZone.of("Asia/Seoul"), "Trip", dates)),
            1,
            mapOf(3 to 1),
            listOf(visit),
        )

    @Test
    fun anOrderFromARemovedDayStaysAvailableForReviewButCannotBeAppliedToUnassignedVisits() {
        val order = TravelVisitChange.Reorder(3, 1, listOf(visit.id))
        val current = TravelPlannerUiState(plan = plan, selectedDay = 3, unappliedOrder = order)
        assertTrue(order.canApplyTo(plan))
        val shortened =
            plan.copy(
                summary = plan.summary.copy(details = plan.summary.details.copy(dates = TravelDates(dates.start, dates.start))),
                visits = listOf(visit.copy(dayNumber = TravelPlan.UNASSIGNED_DAY)),
                revision = 2,
                structureRevision = 2,
            )
        val refreshed = current.withRefreshedPlan(shortened)
        assertEquals(TravelPlan.UNASSIGNED_DAY, refreshed.selectedDay)
        assertEquals(order, refreshed.unappliedOrder)
        assertTrue(order.isOutsideTrip(shortened))
        assertFalse(order.canApplyTo(shortened))
    }

    @Test
    fun inspectionFollowsTheConfirmedVisitAndClosesWhenItIsRemoved() {
        val current = TravelPlannerUiState(plan = plan, selectedDay = 3, inspectedVisitId = visit.id)
        val moved = plan.copy(revision = 2, visits = listOf(visit.copy(dayNumber = 2)))
        val refreshed = current.withRefreshedPlan(moved)
        assertEquals(visit.id, refreshed.inspectedVisitId)
        assertEquals(3, refreshed.selectedDay)
        assertEquals(
            2,
            refreshed.plan
                ?.visits
                ?.single()
                ?.dayNumber,
        )
        assertNull(refreshed.withRefreshedPlan(moved.copy(revision = 3, visits = emptyList())).inspectedVisitId)
    }

    @Test
    fun shortenedTripPreservesTheDraftAndRequiresConfirmationBeforeSaving() {
        val draft = TravelVisitDraft(3, visit, note = "My unsaved note")
        val current = TravelPlannerUiState(plan = plan, selectedDay = 3, visitDraft = draft, viewportRequest = 7)
        val shortened =
            plan.copy(
                summary = plan.summary.copy(details = plan.summary.details.copy(dates = TravelDates(dates.start, dates.start))),
                visits = listOf(visit.copy(dayNumber = TravelPlan.UNASSIGNED_DAY)),
                revision = 2,
                structureRevision = 2,
            )
        val refreshed = current.withRefreshedPlan(shortened)
        assertEquals(TravelPlan.UNASSIGNED_DAY, refreshed.selectedDay)
        assertEquals(draft, refreshed.visitDraft)
        assertTrue(refreshed.needsLatestPlan)
        assertEquals(TravelPlanFailure.CONFLICT, refreshed.failure)
        assertTrue(refreshed.dayPlaces.isEmpty())
    }

    @Test
    fun independentEditsKeepTheOriginalBaseAndDoNotStealMapFocus() {
        val draft = TravelVisitDraft(3, visit, note = "My unsaved note")
        val current =
            TravelPlannerUiState(
                plan = plan,
                selectedDay = 3,
                visitDraft = draft,
                selectedVisitId = visit.id,
                mapFocusedVisitId = visit.id,
                viewportRequest = 7,
            )
        val updated =
            plan.copy(
                revision = 2,
                visits = listOf(visit.copy(details = visit.details.copy(localTime = LocalTime(10, 30)), revision = 2)),
            )
        val refreshed = current.withRefreshedPlan(updated)
        assertEquals(draft, refreshed.visitDraft)
        assertEquals(7, refreshed.viewportRequest)
        assertEquals(visit.id, refreshed.selectedVisitId)
        assertEquals(visit.id, refreshed.mapFocusedVisitId)
        assertFalse(refreshed.needsLatestPlan)
    }

    @Test
    fun removedVisitClearsSelectionButKeepsUnsavedInput() {
        val draft = TravelVisitDraft(3, visit, note = "My unsaved note")
        val refreshed =
            TravelPlannerUiState(
                plan = plan,
                selectedDay = 3,
                visitDraft = draft,
                selectedVisitId = visit.id,
                mapFocusedVisitId = visit.id,
            ).withRefreshedPlan(plan.copy(revision = 2, visits = emptyList()))
        assertEquals(draft, refreshed.visitDraft)
        assertEquals(TravelPlanFailure.VISIT_REMOVED, refreshed.failure)
        assertNull(refreshed.selectedVisitId)
        assertNull(refreshed.mapFocusedVisitId)
        assertTrue(refreshed.needsLatestPlan)
    }

    @Test
    fun uncertainSaveKeepsItsRetryWhenTheTripChanges() {
        val refreshed =
            TravelPlannerUiState(
                plan = plan,
                visitDraft = TravelVisitDraft(3, visit),
                canRetrySave = true,
                failure = TravelPlanFailure.CONNECTION,
            ).withRefreshedPlan(plan.copy(revision = 2, structureRevision = 2))
        assertTrue(refreshed.canRetrySave)
        assertEquals(TravelPlanFailure.CONNECTION, refreshed.failure)
        assertFalse(refreshed.needsLatestPlan)
    }

    @Test
    fun explicitRebaseKeepsMyNoteAndUsesTheLatestUnchangedFields() {
        val draft = TravelVisitDraft(3, visit, note = "My unsaved note")
        val latest = visit.copy(dayNumber = 2, revision = 2, details = visit.details.copy(localTime = LocalTime(10, 30)))
        val rebased = draft.rebaseOn(latest)
        assertEquals("My unsaved note", rebased.note)
        assertEquals("10:30", rebased.localTime)
        assertEquals(2, rebased.dayNumber)
        assertEquals(latest, rebased.visit)
    }
}
