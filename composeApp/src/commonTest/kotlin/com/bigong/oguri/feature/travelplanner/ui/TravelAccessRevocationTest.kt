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
    fun usageCollectionRequiresTheDisplayedOnlinePlanAndCannotChangeEditingState() =
        runTest {
            val plans = AccessPlans(plan)
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            viewModel.recordActivity("plan", TravelUsageActivity.PLAN_DISPLAYED)
            advanceUntilIdle()
            assertTrue(plans.activities.isEmpty())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.recordActivity("different", TravelUsageActivity.DAY_SELECTED)
            viewModel.recordActivity("plan", TravelUsageActivity.PLAN_DISPLAYED)
            advanceUntilIdle()
            assertEquals(listOf("plan" to TravelUsageActivity.PLAN_DISPLAYED), plans.activities)
            viewModel.openVisit(null)
            val before = viewModel.state.value
            plans.activityFailure = true
            viewModel.recordActivity("plan", TravelUsageActivity.DIRECTIONS_OPENED)
            advanceUntilIdle()
            assertEquals(before, viewModel.state.value)
            plans.connectionFailure = true
            viewModel.refreshPlan("plan")
            val offline = viewModel.state.value
            viewModel.recordActivity("plan", TravelUsageActivity.PIN_SELECTED)
            advanceUntilIdle()
            assertEquals(offline, viewModel.state.value)
            assertEquals(1, plans.activities.size)
        }

    @Test
    fun addingASearchedPlaceKeepsItsNoteAndClearedCategoryWhenRetrying() =
        runTest {
            val plans = AccessPlans(plan)
            val place =
                TravelPlace("market", "Market", null, TravelCoordinates(1.0, 2.0), emptyList(), suggestedCategory = TravelVisitCategory.SHOPPING)
            val places =
                object : TravelPlaceRepository by DeniedPlaces() {
                    override suspend fun searchInCity(planId: String, query: String, language: String, city: String): List<TravelPlace> =
                        listOf(place)
                }
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery(), places)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.selectDay(2)
            viewModel.openPlaceSearch()
            viewModel.searchPlaces("Market", "en")
            advanceUntilIdle()
            viewModel.selectPlace(place.id)
            assertEquals(TravelVisitCategory.SHOPPING, viewModel.state.value.placeSearch?.selection?.category)
            viewModel.selectPlaceCategory(null)

            plans.saveFailure = TravelPlanFailure.CONNECTION
            viewModel.addSelectedPlace(note = "  Meet at the entrance  ")
            advanceUntilIdle()
            val first = assertIs<TravelVisitChange.Add>(plans.saveAttempts.single())
            assertEquals("Meet at the entrance", first.details.note)
            assertEquals(place.id, first.details.googlePlaceId)
            assertNull(first.details.category)
            assertEquals(2, first.dayNumber)

            plans.saveFailure = null
            viewModel.retrySave()
            advanceUntilIdle()
            assertEquals(listOf<TravelVisitChange>(first, first), plans.saveAttempts)
        }

    @Test
    fun lodgingConfirmationCanAddOnlyThisDayOrIncludeTheAvailableDays() =
        runTest {
            val existing = TravelVisit(
                "existing-lodging",
                2,
                TravelVisitDetails("Existing hotel", null, null, null, category = TravelVisitCategory.LODGING),
                1,
            )
            val plans = AccessPlans(plan.copy(visits = listOf(existing)))
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            viewModel.loadPlan("plan")
            advanceUntilIdle()

            for (includeOtherDays in listOf(false, true)) {
                viewModel.openVisit(null)
                val draft = checkNotNull(viewModel.state.value.visitDraft)
                viewModel.updateVisitDraft(draft.copy(name = "Hotel", category = TravelVisitCategory.LODGING))
                viewModel.applyVisit()
                assertEquals(listOf(3), viewModel.state.value.lodgingAddition?.offeredDays)
                assertEquals(if (includeOtherDays) 1 else 0, plans.saveAttempts.size)

                viewModel.confirmLodgingAddition(includeOtherDays)
                advanceUntilIdle()

                val change = assertIs<TravelVisitChange.Add>(plans.saveAttempts.last())
                assertEquals(1, change.dayNumber)
                assertEquals(if (includeOtherDays) listOf(3) else emptyList(), change.additionalLodgingDays)
                assertNull(viewModel.state.value.lodgingAddition)
            }
        }

    @Test
    fun visitSaveConflictIsRecordedBeforeALaterConfirmedSave() =
        runTest {
            val plans = AccessPlans(plan)
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.openVisit(null)
            viewModel.updateVisitDraft(requireNotNull(viewModel.state.value.visitDraft).copy(name = "Museum"))
            plans.saveFailure = TravelPlanFailure.CONFLICT
            viewModel.applyVisit()
            advanceUntilIdle()
            assertEquals(TravelPlanFailure.CONFLICT, viewModel.state.value.failure)
            assertEquals(listOf("plan" to TravelUsageActivity.VISIT_SAVE_CONFLICT), plans.activities)

            plans.saveFailure = null
            viewModel.loadPlan("plan", rebaseDraft = true)
            advanceUntilIdle()
            assertEquals(1, plans.activities.size)
            viewModel.applyVisit()
            advanceUntilIdle()
            assertEquals(
                listOf("plan" to TravelUsageActivity.VISIT_SAVE_CONFLICT, "plan" to TravelUsageActivity.VISIT_CONFLICT_RECOVERED),
                plans.activities,
            )
            assertNull(viewModel.state.value.failure)
        }

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
    fun reorderKeepsItsDayRevisionAcrossUnrelatedEditsAndRejectsAChangedOrder() =
        runTest {
            val visits = listOf(
                TravelVisit("first", 1, TravelVisitDetails("First", null, null, null, null), 1),
                TravelVisit("second", 1, TravelVisitDetails("Second", null, null, null, null), 1),
            )
            val plans = AccessPlans(plan.copy(visits = visits, dayOrderRevisions = mapOf(1 to 1L)))
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.selectDay(1)
            val originalOrder = listOf("first", "second")
            val movedOrder = originalOrder.reversed()

            plans.currentPlan = plans.currentPlan.copy(revision = 2)
            viewModel.refreshPlan("plan")
            assertTrue(viewModel.reorderVisits(1, 1, 0, movedOrder))
            advanceUntilIdle()

            plans.currentPlan = plans.currentPlan.copy(revision = 3, dayOrderRevisions = mapOf(1 to 3L))
            viewModel.refreshPlan("plan")
            assertFalse(viewModel.reorderVisits(1, 1, 0, movedOrder))
        }

    @Test
    fun orderConflictRequiresAnExplicitSaveThatPreservesNewVisits() =
        runTest {
            val visits = listOf(
                TravelVisit("first", 1, TravelVisitDetails("First", null, null, null, null), 1),
                TravelVisit("second", 1, TravelVisitDetails("Second", null, null, null, null), 1),
            )
            val plans = AccessPlans(plan.copy(visits = visits, dayOrderRevisions = mapOf(1 to 1L)))
            val recovery = AccessRecovery()
            val viewModel = createTravelPlannerTestViewModel(plans, recovery)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.selectDay(1)
            val desired = listOf("second", "first")
            plans.saveFailure = TravelPlanFailure.CONFLICT
            assertTrue(viewModel.reorderVisits(1, 1, 0, desired))
            advanceUntilIdle()
            val attempted = assertNotNull(viewModel.state.value.unappliedOrder)
            assertEquals(desired, attempted.visitIds)
            viewModel.applyUnappliedOrder()
            advanceUntilIdle()
            assertEquals(listOf<TravelVisitChange>(attempted), plans.saveAttempts)

            plans.saveFailure = null
            val addedVisit = TravelVisit("added", 1, TravelVisitDetails("Added", null, null, null, null), 1)
            plans.currentPlan = plans.currentPlan.copy(revision = 2, dayOrderRevisions = mapOf(1 to 2L), visits = visits + addedVisit)
            val reopened = createTravelPlannerTestViewModel(plans, recovery)
            reopened.loadPlan("plan")
            advanceUntilIdle()
            reopened.selectDay(1)
            assertEquals(attempted, reopened.state.value.unappliedOrder)
            assertFalse(reopened.state.value.canRetrySave)
            assertEquals(1, plans.saveAttempts.size)
            reopened.applyUnappliedOrder()
            advanceUntilIdle()
            assertEquals(
                listOf<TravelVisitChange>(attempted, attempted.copy(expectedOrderRevision = 2, visitIds = desired + addedVisit.id)),
                plans.saveAttempts,
            )
            assertEquals(2, plans.saveRequestIds.distinct().size)
            assertNull(reopened.state.value.unappliedOrder)
            assertNull(recovery.load("plan").unappliedOrder)
        }

    @Test
    fun automaticRefreshSkipsUnchangedCacheWritesAndPreservesTheDraftWhenARevisionArrives() =
        runTest {
            val plans = AccessPlans(plan)
            val recovery = AccessRecovery()
            val viewModel = createTravelPlannerTestViewModel(plans, recovery)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.openVisit(null)
            val draft = requireNotNull(viewModel.state.value.visitDraft).copy(name = "Museum")
            viewModel.updateVisitDraft(draft)
            advanceUntilIdle()

            viewModel.refreshPlan("plan")
            assertEquals(listOf(plan.revision), plans.requestedRevisions)
            assertEquals(1, recovery.confirmedWrites)
            assertEquals(draft, viewModel.state.value.visitDraft)

            val edited = plan.copy(revision = plan.revision + 1)
            plans.currentPlan = edited
            viewModel.refreshPlan("plan")
            assertEquals(edited.revision, viewModel.state.value.plan?.revision)
            assertEquals(edited, recovery.confirmed)
            assertEquals(draft, viewModel.state.value.visitDraft)
            assertEquals(2, recovery.confirmedWrites)
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
    fun homeReentryReusesTheResumeResultUntilInvalidated() =
        runTest {
            var requests = 0
            val plans =
                ResumePlans(plan).apply {
                    action = {
                        requests++
                        plan.summary
                    }
                }
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())

            viewModel.refreshResumeIfNeeded()
            viewModel.refreshResumeIfNeeded()
            assertEquals(1, requests)
            assertEquals(plan.summary, viewModel.state.value.resumePlan)

            viewModel.invalidateResume(removedPlanId = plan.summary.id)
            assertNull(viewModel.state.value.resumePlan)
            plans.action = {
                requests++
                null
            }
            viewModel.refreshResumeIfNeeded()
            viewModel.refreshResumeIfNeeded()
            assertEquals(2, requests)
            assertNull(viewModel.state.value.resumePlan)
        }

    @Test
    fun aLateResumeResponseCannotReplaceTheLatestHomeCard() =
        runTest {
            val barrier = CompletableDeferred<Unit>()
            val plans =
                ResumePlans(plan).apply {
                    action = {
                        withContext(NonCancellable) { barrier.await() }
                        plan.summary
                    }
                }
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            launch { viewModel.loadResume() }
            runCurrent()
            val latest = plan.summary.copy(id = "latest")
            plans.action = { latest }
            viewModel.loadResume()
            barrier.complete(Unit)
            advanceUntilIdle()
            assertEquals(latest, viewModel.state.value.resumePlan)
        }

    @Test
    fun aCancelledHomeRefreshCannotPublishItsLateResult() =
        runTest {
            val barrier = CompletableDeferred<Unit>()
            val plans =
                ResumePlans(plan).apply {
                    action = {
                        withContext(NonCancellable) { barrier.await() }
                        plan.summary
                    }
                }
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            val reading = launch { viewModel.loadResume() }
            runCurrent()
            reading.cancel()
            barrier.complete(Unit)
            advanceUntilIdle()
            assertNull(viewModel.state.value.resumePlan)
        }

    @Test
    fun accessRevocationCannotBeUndoneByAPendingHomeRefresh() =
        runTest {
            val barrier = CompletableDeferred<Unit>()
            val plans = ResumePlans(plan)
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            viewModel.loadResume()
            assertEquals(plan.summary, viewModel.state.value.resumePlan)
            plans.action = {
                withContext(NonCancellable) { barrier.await() }
                plan.summary
            }
            launch { viewModel.loadResume() }
            runCurrent()
            viewModel.forgetUnavailablePlan(plan.summary.id)
            barrier.complete(Unit)
            advanceUntilIdle()
            assertNull(viewModel.state.value.resumePlan)
            assertEquals(TravelPlanFailure.UNAVAILABLE, viewModel.state.value.failure)
        }

    @Test
    fun emptyAndDeniedResumeLookupsHideTheCardWithoutFailingTheHomePage() =
        runTest {
            val plans = ResumePlans(plan)
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            viewModel.loadResume()
            plans.action = { null }
            viewModel.loadResume()
            assertNull(viewModel.state.value.resumePlan)
            plans.action = { plan.summary }
            viewModel.loadResume()
            plans.action = { throw TravelPlanException(TravelPlanFailure.SIGN_IN_REQUIRED) }
            viewModel.loadResume()
            assertNull(viewModel.state.value.resumePlan)
            assertNull(viewModel.state.value.failure)
        }

    @Test
    fun reloadingAShortenedTripUsesTheUnassignedDayAndKeepsTheDraft() =
        runTest {
            val plans = AccessPlans(plan)
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery())
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.selectDay(3)
            viewModel.openVisit(null)
            val draft = requireNotNull(viewModel.state.value.visitDraft).copy(name = "Museum")
            viewModel.updateVisitDraft(draft)
            advanceUntilIdle()

            val details = plan.summary.details
            plans.currentPlan = plan.copy(
                summary = plan.summary.copy(details = details.copy(dates = TravelDates(details.dates.start, details.dates.start))),
                revision = 2,
                structureRevision = 1,
            )
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            assertEquals(1, viewModel.state.value.selectedDay)
            assertEquals(draft, viewModel.state.value.visitDraft)
            assertTrue(viewModel.state.value.needsLatestPlan)

            viewModel.loadPlan("plan", rebaseDraft = true)
            advanceUntilIdle()
            assertEquals(1, viewModel.state.value.visitDraft?.dayNumber)
            assertEquals("Museum", viewModel.state.value.visitDraft?.name)
            assertTrue(plans.saveAttempts.isEmpty())
        }

    @Test
    fun anUncertainSaveCanBeRetriedWithTheSameRequestAfterLeavingTheTrip() =
        runTest {
            val plans = AccessPlans(plan)
            val recovery = AccessRecovery()
            val editing = createTravelPlannerTestViewModel(plans, recovery)
            editing.loadPlan("plan")
            advanceUntilIdle()
            editing.openVisit(null)
            editing.updateVisitDraft(requireNotNull(editing.state.value.visitDraft).copy(name = "Museum"))
            plans.saveFailure = TravelPlanFailure.CONNECTION
            editing.applyVisit()
            advanceUntilIdle()
            assertTrue(editing.state.value.canRetrySave)
            assertTrue(editing.state.value.pendingSaveStored)
            val saved = assertIs<TravelPendingSave.Visit>(recovery.load("plan").pending)
            assertEquals(listOf(saved.requestId), plans.saveRequestIds)

            val reopened = createTravelPlannerTestViewModel(plans, recovery)
            reopened.loadPlan("plan")
            advanceUntilIdle()
            assertTrue(reopened.state.value.canRetrySave)
            assertTrue(reopened.state.value.pendingSaveStored)
            assertEquals(listOf(saved.requestId), plans.saveRequestIds)

            plans.saveFailure = null
            reopened.retrySave()
            advanceUntilIdle()
            assertEquals(listOf(saved.requestId, saved.requestId), plans.saveRequestIds)
            assertFalse(reopened.state.value.pendingSaveStored)
            assertNull(recovery.load("plan").pending)
        }

    @Test
    fun anExpiredSnapshotDoesNotHideTheDraftAfterTheConnectionReturns() =
        runTest {
            val plans = AccessPlans(plan)
            val recovery = AccessRecovery()
            val editing = createTravelPlannerTestViewModel(plans, recovery)
            editing.loadPlan("plan")
            advanceUntilIdle()
            editing.openVisit(null)
            editing.updateVisitDraft(requireNotNull(editing.state.value.visitDraft).copy(name = "Museum", note = "My draft"))
            advanceUntilIdle()
            recovery.confirmed = null
            plans.connectionFailure = true

            val reopened = createTravelPlannerTestViewModel(plans, recovery)
            reopened.loadPlan("plan")
            advanceUntilIdle()
            assertNull(reopened.state.value.plan)
            assertTrue(reopened.state.value.isOffline)
            assertEquals("My draft", reopened.state.value.visitDraft?.note)

            plans.connectionFailure = false
            reopened.refreshPlan("plan")
            advanceUntilIdle()
            assertEquals(plan, reopened.state.value.plan)
            assertEquals("My draft", reopened.state.value.visitDraft?.note)
            assertFalse(reopened.state.value.isOffline)
            assertTrue(plans.saveAttempts.isEmpty())
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

    private fun overviewPlan(count: Int): TravelPlan =
        plan.copy(
            visits =
                List(count) { index ->
                    TravelVisit("visit-$index", index / 30 + 1, TravelVisitDetails(null, null, null, null, "place-$index"), 1)
                },
        )

    @Test
    fun overviewSplitsRequestsAndRetainsEveryBatch() =
        runTest {
            val places = BatchOverviewPlaces()
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(overviewPlan(65)), AccessRecovery(), places)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.loadOverviewPlaces("plan", "ko")
            assertEquals(listOf(30, 30, 5), places.requests.map { it.size })
            assertEquals(
                65,
                viewModel.state.value.overview
                    ?.places
                    ?.size,
            )
            assertFalse(requireNotNull(viewModel.state.value.overview).incomplete)
        }

    @Test
    fun quotaFailureKeepsPreviousBatchesAndRetryOnlyRequestsMissingPlaces() =
        runTest {
            val places = BatchOverviewPlaces().apply { failAtCall = 2 }
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(overviewPlan(65)), AccessRecovery(), places)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.loadOverviewPlaces("plan", "ko")
            val partial = requireNotNull(viewModel.state.value.overview)
            assertEquals(30, partial.places.size)
            assertTrue(partial.incomplete)
            assertFalse(partial.loading)
            places.failAtCall = null
            viewModel.loadOverviewPlaces("plan", "ko")
            assertEquals(
                65,
                viewModel.state.value.overview
                    ?.places
                    ?.size,
            )
            assertFalse(requireNotNull(viewModel.state.value.overview).incomplete)
            assertTrue(
                places.requests
                    .drop(2)
                    .flatten()
                    .none { it in partial.places },
            )
            assertEquals(listOf(30, 5), places.requests.drop(2).map { it.size })
        }

    @Test
    fun placeDetailsKeepLoadedPinsOfflineAndFetchMissingPinsAfterReconnect() =
        runTest {
            val plans = AccessPlans(overviewPlan(2))
            val places = BatchOverviewPlaces().apply { dayIds = listOf("place-0") }
            val viewModel = createTravelPlannerTestViewModel(plans, AccessRecovery(), places)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.loadDayPlaces("plan", 1, "ko", hasPlaces = true)

            plans.connectionFailure = true
            viewModel.refreshPlan("plan")
            viewModel.loadDayPlaces("plan", 1, "ko", hasPlaces = true)
            viewModel.loadOverviewPlaces("plan", "ko")
            val offline = viewModel.state.value
            assertEquals(setOf("place-0"), offline.dayPlaces.keys)
            assertEquals(setOf("place-0"), offline.overview?.places?.keys)
            assertTrue(requireNotNull(offline.overview).incomplete)
            assertFalse(offline.overview.loading)
            assertEquals(1, places.dayRequests)
            assertTrue(places.requests.isEmpty())

            plans.connectionFailure = false
            viewModel.refreshPlan("plan")
            advanceUntilIdle()
            viewModel.loadDayPlaces("plan", 1, "ko", hasPlaces = true)
            viewModel.loadOverviewPlaces("plan", "ko")
            assertEquals(2, places.dayRequests)
            assertEquals(listOf(listOf("place-1")), places.requests)
            assertFalse(requireNotNull(viewModel.state.value.overview).incomplete)
        }

    @Test
    fun overviewReusesTheSameLanguageDayCacheButReloadsForANewLanguage() =
        runTest {
            val places = BatchOverviewPlaces().apply { dayIds = (0 until 30).map { "place-$it" } }
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(overviewPlan(65)), AccessRecovery(), places)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            viewModel.loadDayPlaces("plan", 1, "ko", hasPlaces = true)
            viewModel.loadOverviewPlaces("plan", "ko")
            assertEquals(listOf(30, 5), places.requests.map { it.size })
            assertTrue(places.requests.flatten().none { it in places.dayIds })
            places.requests.clear()
            viewModel.loadOverviewPlaces("plan", "en")
            assertEquals(listOf(30, 30, 5), places.requests.map { it.size })
            assertTrue(requireNotNull(viewModel.state.value.overview).places.values.all { it.name.startsWith("en:") })
        }

    @Test
    fun anOldFailureCannotReplaceTheSuccessfullyReopenedOverview() =
        runTest {
            val barrier = CompletableDeferred<Unit>()
            val places =
                BatchOverviewPlaces().apply {
                    firstBarrier = barrier
                    failAtCall = 1
                }
            val viewModel = createTravelPlannerTestViewModel(AccessPlans(overviewPlan(1)), AccessRecovery(), places)
            viewModel.loadPlan("plan")
            advanceUntilIdle()
            launch { viewModel.loadOverviewPlaces("plan", "ko") }
            runCurrent()
            viewModel.closeOverview()
            viewModel.loadOverviewPlaces("plan", "ko")
            barrier.complete(Unit)
            advanceUntilIdle()
            val overview = requireNotNull(viewModel.state.value.overview)
            assertEquals(1, overview.places.size)
            assertFalse(overview.incomplete)
            assertFalse(overview.loading)
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
    @Test
    fun presetCityKeepsItsZoneWithoutAPlaceLookup() = runTest {
        val viewModel = createTravelPlannerTestViewModel(AccessPlans(plan), AccessRecovery(), DeniedPlaces())
        assertEquals("Asia/Seoul", viewModel.resolveCityTimeZone("서울", "ko"))
    }

    @Test
    fun promotedSavedCityResolvesItsOwnZone() = runTest {
        val places = object : TravelPlaceRepository by DeniedPlaces() {
            override suspend fun cities(query: String, language: String): List<TravelCitySuggestion> {
                assertEquals("싱가포르", query)
                assertEquals("ko", language)
                return listOf(TravelCitySuggestion("singapore-id", query))
            }
            override suspend fun cityTimeZone(placeId: String): String {
                assertEquals("singapore-id", placeId)
                return "Asia/Singapore"
            }
        }
        val viewModel = createTravelPlannerTestViewModel(AccessPlans(plan), AccessRecovery(), places)
        assertEquals("Asia/Singapore", viewModel.resolveCityTimeZone("싱가포르", "ko"))
    }

    @Test
    fun anotherCitySearchResultCannotSupplyThePromotedCityZone() = runTest {
        val places = object : TravelPlaceRepository by DeniedPlaces() {
            override suspend fun cities(query: String, language: String) = listOf(TravelCitySuggestion("other-id", "다른 도시"))
        }
        val viewModel = createTravelPlannerTestViewModel(AccessPlans(plan), AccessRecovery(), places)
        assertNull(viewModel.resolveCityTimeZone("싱가포르", "ko"))
    }

    @Test
    fun failedCityLookupCanBeRetriedWithoutKeepingAnInvalidZone() = runTest {
        var fail = true
        val places = object : TravelPlaceRepository by DeniedPlaces() {
            override suspend fun cities(query: String, language: String): List<TravelCitySuggestion> {
                if (fail) throw TravelPlanException(TravelPlanFailure.CONNECTION)
                return listOf(TravelCitySuggestion("singapore-id", query))
            }
            override suspend fun cityTimeZone(placeId: String) = "Asia/Singapore"
        }
        val viewModel = createTravelPlannerTestViewModel(AccessPlans(plan), AccessRecovery(), places)
        assertNull(viewModel.resolveCityTimeZone("싱가포르", "ko"))
        fail = false
        assertEquals("Asia/Singapore", viewModel.resolveCityTimeZone("싱가포르", "ko"))
    }

}

private class AccessPlans(
    var currentPlan: TravelPlan,
) : TravelPlanRepository {
    val requestedRevisions = mutableListOf<Long>()
    val activities = mutableListOf<Pair<String, TravelUsageActivity>>()
    var activityFailure = false

    override suspend fun recordActivity(
        planId: String,
        kind: TravelUsageActivity,
    ) {
        if (activityFailure) throw TravelPlanException(TravelPlanFailure.CONNECTION)
        activities.add(planId to kind)
    }

    var unavailable: Boolean = false
    var connectionFailure: Boolean = false
    val saveAttempts = mutableListOf<TravelVisitChange>()
    val saveRequestIds = mutableListOf<String>()
    var saveFailure: TravelPlanFailure? = null
    var readBarrier: CompletableDeferred<Unit>? = null

    override suspend fun get(planId: String): TravelPlan {
        readBarrier?.await()
        if (unavailable) throw TravelPlanException(TravelPlanFailure.UNAVAILABLE)
        if (connectionFailure) throw TravelPlanException(TravelPlanFailure.CONNECTION)
        return currentPlan
    }

    override suspend fun getIfChanged(
        planId: String,
        knownRevision: Long,
    ): TravelPlan? {
        requestedRevisions.add(knownRevision)
        return get(planId).takeUnless { it.revision == knownRevision }
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
    ): TravelPlan {
        saveAttempts.add(change)
        saveRequestIds.add(requestId)
        saveFailure?.let { throw TravelPlanException(it) }
        return currentPlan
    }
}

private class AccessRecovery : TravelRecoveryRepository {
    var confirmed: TravelPlan? = null
    private var pending: TravelPendingSave? = null
    private var unappliedOrder: TravelVisitChange.Reorder? = null
    var confirmedWrites: Int = 0
    var draft: TravelDraft? = null
    var forgotten: Boolean = false
    var draftWrites: Int = 0
    var draftBarrier: CompletableDeferred<Unit>? = null

    override suspend fun load(planId: String?): TravelRecovery = TravelRecovery(confirmedPlan = confirmed, draft = draft, pending = pending, unappliedOrder = unappliedOrder)

    override suspend fun rememberPlan(plan: TravelPlan) {
        confirmedWrites++
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
        pending = null
        unappliedOrder = null
        forgotten = true
    }

    override suspend fun saveRequest(
        planId: String?,
        request: TravelPendingSave,
        draft: TravelDraft?,
    ) {
        pending = request
    }

    override suspend fun discardUnappliedOrder(planId: String) {
        unappliedOrder = null
    }

    override suspend fun finishRequest(
        planId: String?,
        clearDraft: Boolean,
        conflictedOrder: TravelVisitChange.Reorder?,
    ) {
        val submitted = (pending as? TravelPendingSave.Visit)?.change as? TravelVisitChange.Reorder
        if (conflictedOrder != null) {
            unappliedOrder = conflictedOrder
        } else if (clearDraft && submitted?.dayNumber == unappliedOrder?.dayNumber) {
            unappliedOrder = null
        }
        pending = null
    }
}

private class DeniedPlaces : TravelPlaceRepository {
    override suspend fun forPlan(
        planId: String,
        language: String,
        placeIds: List<String>,
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
        placeIds: List<String>,
    ): TravelDayPlaces {
        withContext(NonCancellable) { barrier?.await() }
        if (fail) throw TravelPlanException(TravelPlanFailure.CONNECTION)
        return TravelDayPlaces(mapOf("place" to TravelPlace("place", "Museum", null, TravelCoordinates(37.5, 127.0), emptyList())), false)
    }
}

private class BatchOverviewPlaces : TravelPlaceRepository by DeniedPlaces() {
    val requests: MutableList<List<String>> = mutableListOf()
    var failAtCall: Int? = null
    var firstBarrier: CompletableDeferred<Unit>? = null
    var dayIds: List<String> = emptyList()
    var dayRequests: Int = 0

    override suspend fun forPlan(
        planId: String,
        language: String,
        placeIds: List<String>,
    ): TravelDayPlaces {
        requests.add(placeIds)
        val call = requests.size
        if (call == 1) withContext(NonCancellable) { firstBarrier?.await() }
        if (call == failAtCall) throw TravelPlanException(TravelPlanFailure.RATE_LIMIT)
        return response(placeIds, language)
    }

    override suspend fun forDay(
        planId: String,
        dayNumber: Int,
        language: String,
    ): TravelDayPlaces {
        dayRequests++
        return response(dayIds, language)
    }

    private fun response(
        ids: List<String>,
        language: String,
    ): TravelDayPlaces =
        TravelDayPlaces(
            ids.associateWith { TravelPlace(it, "$language:$it", null, TravelCoordinates(37.5, 127.0), emptyList()) },
            false,
        )
}

private class ResumePlans(
    plan: TravelPlan,
) : TravelPlanRepository by AccessPlans(plan) {
    var action: suspend () -> TravelPlanSummary? = { plan.summary }

    override suspend fun resume(): TravelPlanSummary? = action()
}
