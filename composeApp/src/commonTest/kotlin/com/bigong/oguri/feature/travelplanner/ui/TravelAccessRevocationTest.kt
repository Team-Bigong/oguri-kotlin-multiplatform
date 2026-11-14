package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.domain.model.*
import com.bigong.oguri.domain.repository.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TravelAccessRevocationTest {
    private val plan =
        TravelPlan(
            TravelPlanSummary(
                "plan",
                TravelPlanDetails("Seoul", TimeZone.of("Asia/Seoul"), "Trip", TravelDates(LocalDate(2027, 4, 1), LocalDate(2027, 4, 3))),
            ),
            1,
            emptyMap(),
            emptyList(),
        )

    @BeforeTest
    fun prepareDispatcher() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun resetDispatcher() = Dispatchers.resetMain()

    @Test
    fun revokedAccessWaitsForAnActiveDraftWriteBeforeErasingStorage() =
        runTest {
            val plans = AccessPlans(plan)
            val recovery = AccessRecovery().apply { draftBarrier = CompletableDeferred() }
            val viewModel = createTravelPlannerTestViewModel(plans, recovery)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.openVisit(null)
            viewModel.updateVisitDraft(requireNotNull(viewModel.state.value.visitDraft).copy(name = "Secret place"))
            runCurrent()
            plans.unavailable = true
            launch { viewModel.refreshPlan("plan") }
            runCurrent()
            assertNull(viewModel.state.value.plan)
            assertNull(viewModel.state.value.visitDraft)
            assertFalse(recovery.forgotten)
            recovery.draftBarrier?.complete(Unit)
            advanceUntilIdle()
            assertTrue(recovery.forgotten)
            assertNull(recovery.draft)
            assertNull(recovery.confirmed)
            assertEquals(TravelPlanFailure.UNAVAILABLE, viewModel.state.value.failure)
        }

    @Test
    fun deniedViewRecordingAlsoErasesThePlanImmediately() =
        runTest {
            val plans = AccessPlans(plan)
            val recovery = AccessRecovery()
            val viewModel = createTravelPlannerTestViewModel(plans, recovery)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            plans.unavailable = true
            viewModel.recordView("plan", 1)
            assertNull(viewModel.state.value.plan)
            assertTrue(recovery.forgotten)
            assertNull(recovery.confirmed)
        }

    @Test
    fun aLateSuccessfulRefreshCannotRestoreAnAlreadyRevokedPlan() =
        runTest {
            val plans = AccessPlans(plan)
            val recovery = AccessRecovery()
            val viewModel = createTravelPlannerTestViewModel(plans, recovery, DeniedPlaces())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            plans.readBarrier = CompletableDeferred()
            launch { viewModel.refreshPlan("plan") }
            runCurrent()
            viewModel.loadDayPlaces("plan", 1, "ko", hasPlaces = true)
            assertTrue(recovery.forgotten)
            plans.readBarrier?.complete(Unit)
            advanceUntilIdle()
            assertNull(viewModel.state.value.plan)
            assertNull(recovery.confirmed)
            assertEquals(TravelPlanFailure.UNAVAILABLE, viewModel.state.value.failure)
        }

    @Test
    fun offlineInspectionReadsTheConfirmedNoteWithoutCreatingOrClearingADraft() =
        runTest {
            val visit = TravelVisit("visit", 1, TravelVisitDetails("Museum", "Address", null, "Confirmed shared note"), 1)
            val withVisit = plan.copy(visits = listOf(visit))
            val plans = AccessPlans(withVisit)
            val recovery = AccessRecovery()
            val viewModel = createTravelPlannerTestViewModel(plans, recovery)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            plans.connectionFailure = true
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            assertTrue(viewModel.state.value.isOffline)
            val writesBefore = recovery.draftWrites
            viewModel.inspectVisit(visit.id)
            assertEquals(visit.id, viewModel.state.value.inspectedVisitId)
            assertEquals(visit.id, viewModel.state.value.selectedVisitId)
            assertEquals(
                "Confirmed shared note",
                viewModel.state.value.plan
                    ?.visits
                    ?.single()
                    ?.details
                    ?.note,
            )
            assertNull(viewModel.state.value.visitDraft)
            viewModel.closeInspection()
            advanceUntilIdle()
            assertNull(viewModel.state.value.inspectedVisitId)
            assertNull(recovery.draft)
            assertEquals(writesBefore, recovery.draftWrites)
        }

    @Test
    fun inspectionCannotReplaceAnExistingUnsavedDraft() =
        runTest {
            val visit = TravelVisit("visit", 1, TravelVisitDetails("Museum", null, null, null), 1)
            val recovery = AccessRecovery()
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(plan.copy(visits = listOf(visit))), recovery)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.openVisit(null)
            val draft = requireNotNull(viewModel.state.value.visitDraft).copy(name = "New place", note = "Unsaved note")
            viewModel.updateVisitDraft(draft)
            advanceUntilIdle()
            viewModel.inspectVisit(visit.id)
            viewModel.closeInspection()
            assertNull(viewModel.state.value.inspectedVisitId)
            assertEquals(draft, viewModel.state.value.visitDraft)
            assertEquals("Unsaved note", recovery.draft?.note)
        }

    @Test
    fun revokedAccessAlsoClosesInspectionAndRejectsLateSelection() =
        runTest {
            val visit = TravelVisit("visit", 1, TravelVisitDetails("Museum", null, null, "Shared note"), 1)
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(plan.copy(visits = listOf(visit))), AccessRecovery())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.inspectVisit("missing")
            assertNull(viewModel.state.value.inspectedVisitId)
            viewModel.inspectVisit(visit.id)
            assertEquals(visit.id, viewModel.state.value.inspectedVisitId)
            viewModel.forgetUnavailablePlan("plan")
            viewModel.inspectVisit(visit.id)
            assertNull(viewModel.state.value.inspectedVisitId)
            assertNull(viewModel.state.value.plan)
        }

    @Test
    fun loadedOverviewUsesSeparatePlacesAndDoesNotChangeTheSelectedDay() =
        runTest {
            val withPlace = plan.copy(visits = listOf(TravelVisit("visit", 2, TravelVisitDetails(null, null, null, null, "place"), 1)))
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(withPlace), AccessRecovery(), OverviewPlaces())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            val selectedDay = viewModel.state.value.selectedDay
            viewModel.loadOverviewPlaces("plan", "ko")
            val overview = requireNotNull(viewModel.state.value.overview)
            assertEquals("Museum", overview.places["place"]?.name)
            assertFalse(overview.loading)
            assertFalse(overview.incomplete)
            assertTrue(
                viewModel.state.value.dayPlaces
                    .isEmpty(),
            )
            assertEquals(selectedDay, viewModel.state.value.selectedDay)
        }

    @Test
    fun lateOverviewResponseCannotRestoreDataAfterAccessIsRevoked() =
        runTest {
            val barrier = CompletableDeferred<Unit>()
            val recovery = AccessRecovery()
            val withPlace = plan.copy(visits = listOf(TravelVisit("visit", 2, TravelVisitDetails(null, null, null, null, "place"), 1)))
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(withPlace), recovery, OverviewPlaces(barrier))
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            launch { viewModel.loadOverviewPlaces("plan", "ko") }
            runCurrent()
            viewModel.forgetUnavailablePlan("plan")
            barrier.complete(Unit)
            advanceUntilIdle()
            assertNull(viewModel.state.value.plan)
            assertNull(viewModel.state.value.overview)
            assertTrue(recovery.forgotten)
        }

    @Test
    fun overviewAccessDenialErasesBothOverviewAndConfirmedPlan() =
        runTest {
            val recovery = AccessRecovery()
            val withPlace = plan.copy(visits = listOf(TravelVisit("visit", 2, TravelVisitDetails(null, null, null, null, "place"), 1)))
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(withPlace), recovery, DeniedPlaces())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.loadOverviewPlaces("plan", "ko")
            assertNull(viewModel.state.value.plan)
            assertNull(viewModel.state.value.overview)
            assertTrue(recovery.forgotten)
        }

    @Test
    fun lateOverviewResponseCannotReopenAClosedSheet() =
        runTest {
            val barrier = CompletableDeferred<Unit>()
            val places = OverviewPlaces(barrier)
            val withPlace = plan.copy(visits = listOf(TravelVisit("visit", 2, TravelVisitDetails(null, null, null, null, "place"), 1)))
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(withPlace), AccessRecovery(), places)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            launch { viewModel.loadOverviewPlaces("plan", "ko") }
            runCurrent()
            assertNotNull(viewModel.state.value.overview)
            viewModel.closeOverview()
            barrier.complete(Unit)
            advanceUntilIdle()
            assertNull(viewModel.state.value.overview)
            assertEquals(withPlace, viewModel.state.value.plan)
        }

    @Test
    fun overviewSelectionReturnsToTheActualDayWithoutOpeningTheEditor() =
        runTest {
            val visits =
                listOf(
                    TravelVisit("second-day", 2, TravelVisitDetails("Museum", null, null, null), 1),
                    TravelVisit("undated", 0, TravelVisitDetails("Park", null, null, null), 1),
                )
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(plan.copy(visits = visits)), AccessRecovery())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.selectOverviewVisit("second-day")
            assertEquals(2, viewModel.state.value.selectedDay)
            assertEquals("second-day", viewModel.state.value.selectedVisitId)
            assertEquals("second-day", viewModel.state.value.mapFocusedVisitId)
            assertNull(viewModel.state.value.visitDraft)
            viewModel.selectOverviewVisit("undated")
            assertEquals(0, viewModel.state.value.selectedDay)
            assertEquals("undated", viewModel.state.value.selectedVisitId)
            assertNull(viewModel.state.value.visitDraft)
        }

    @Test
    fun overviewProviderFailureKeepsThePlanAndMakesTheOverviewRetryable() =
        runTest {
            val withPlace = plan.copy(visits = listOf(TravelVisit("visit", 2, TravelVisitDetails(null, null, null, null, "place"), 1)))
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(withPlace), AccessRecovery(), OverviewPlaces(fail = true))
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.loadOverviewPlaces("plan", "ko")
            assertEquals(withPlace, viewModel.state.value.plan)
            assertTrue(requireNotNull(viewModel.state.value.overview).incomplete)
            assertFalse(requireNotNull(viewModel.state.value.overview).loading)
        }
}

private class AccessPlans(
    private val plan: TravelPlan,
) : TravelPlanRepository {
    var unavailable: Boolean = false
    var connectionFailure: Boolean = false
    var readBarrier: CompletableDeferred<Unit>? = null

    override suspend fun get(planId: String): TravelPlan {
        readBarrier?.await()
        if (unavailable) throw TravelPlanException(TravelPlanFailure.UNAVAILABLE)
        if (connectionFailure) throw TravelPlanException(TravelPlanFailure.CONNECTION)
        return plan
    }

    override suspend fun list(): List<TravelPlanSummary> = error("Unused")

    override suspend fun resume(): TravelPlanSummary? = error("Unused")

    override suspend fun recordView(
        planId: String,
        dayNumber: Int,
    ) {
        if (unavailable) throw TravelPlanException(TravelPlanFailure.UNAVAILABLE)
    }

    override suspend fun create(
        requestId: String,
        details: TravelPlanDetails,
    ): TravelPlanSummary = error("Unused")

    override suspend fun changeVisit(
        planId: String,
        requestId: String,
        change: TravelVisitChange,
    ): TravelPlan = error("Unused")
}

private class AccessRecovery : TravelRecoveryRepository {
    var confirmed: TravelPlan? = null
    var draft: TravelDraft? = null
    var forgotten: Boolean = false
    var draftWrites: Int = 0
    var draftBarrier: CompletableDeferred<Unit>? = null

    override suspend fun load(planId: String?): TravelRecovery = TravelRecovery(confirmedPlan = confirmed, draft = draft)

    override suspend fun rememberPlan(plan: TravelPlan) {
        confirmed = plan
    }

    override suspend fun saveDraft(
        planId: String,
        draft: TravelDraft?,
    ) {
        withContext(NonCancellable) {
            draftBarrier?.await()
            draftWrites++
            this@AccessRecovery.draft = draft
        }
    }

    override suspend fun forget(planId: String) {
        confirmed = null
        draft = null
        forgotten = true
    }

    override suspend fun saveRequest(
        planId: String?,
        request: TravelPendingSave,
        draft: TravelDraft?,
    ): Unit = error("Unused")

    override suspend fun finishRequest(
        planId: String?,
        clearDraft: Boolean,
    ): Unit = error("Unused")
}

private class DeniedPlaces : TravelPlaceRepository {
    override suspend fun forPlan(
        planId: String,
        language: String,
    ): TravelDayPlaces = throw TravelPlanException(TravelPlanFailure.UNAVAILABLE)

    override suspend fun forDay(
        planId: String,
        dayNumber: Int,
        language: String,
    ): TravelDayPlaces = throw TravelPlanException(TravelPlanFailure.UNAVAILABLE)

    override suspend fun cities(
        query: String,
        language: String,
    ): List<TravelCitySuggestion> = error("Unused")

    override suspend fun cityTimeZone(placeId: String): String? = error("Unused")

    override suspend fun search(
        planId: String,
        query: String,
        language: String,
    ): List<TravelPlace> = error("Unused")
}

private class OverviewPlaces(
    private val barrier: CompletableDeferred<Unit>? = null,
    private val fail: Boolean = false,
) : TravelPlaceRepository by DeniedPlaces() {
    override suspend fun forPlan(
        planId: String,
        language: String,
    ): TravelDayPlaces {
        withContext(NonCancellable) { barrier?.await() }
        if (fail) throw TravelPlanException(TravelPlanFailure.CONNECTION)
        return TravelDayPlaces(mapOf("place" to TravelPlace("place", "Museum", null, TravelCoordinates(37.5, 127.0), emptyList())), false)
    }
}
