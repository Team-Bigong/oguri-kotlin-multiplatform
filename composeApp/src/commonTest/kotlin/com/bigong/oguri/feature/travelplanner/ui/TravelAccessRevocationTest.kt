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
}

private class AccessPlans(
    private val plan: TravelPlan,
) : TravelPlanRepository {
    var unavailable: Boolean = false
    var readBarrier: CompletableDeferred<Unit>? = null

    override suspend fun get(planId: String): TravelPlan {
        readBarrier?.await()
        if (unavailable) throw TravelPlanException(TravelPlanFailure.UNAVAILABLE)
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
