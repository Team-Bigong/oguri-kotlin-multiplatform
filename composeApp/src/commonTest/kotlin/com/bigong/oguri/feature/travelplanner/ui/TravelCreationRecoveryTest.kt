package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.domain.model.*
import com.bigong.oguri.domain.repository.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TravelCreationRecoveryTest {
    private val details =
        TravelPlanDetails(
            "도쿄",
            TimeZone.of("Asia/Tokyo"),
            "첫 여행",
            TravelDates(LocalDate(2027, 4, 1), LocalDate(2027, 4, 3)),
            TravelPlanSource(1, LocalDate(2027, 4, 1), LocalDate(2027, 4, 5)),
        )

    @BeforeTest
    fun prepareDispatcher() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun resetDispatcher() = Dispatchers.resetMain()

    @Test
    fun delayedRecoveryBlocksNewCreationAndRestoresTheOriginalRequest() =
        runTest {
            val plans = CreationPlans()
            val pending = TravelPendingSave.Create("original-request", details)
            val recovery = CreationRecovery(pending).apply { barrier = CompletableDeferred() }
            val viewModel = createTravelPlannerTestViewModel(plans, recovery)
            launch { viewModel.recoverCreation() }
            runCurrent()
            viewModel.create(details.copy(title = "Another trip"))
            runCurrent()
            assertTrue(plans.requests.isEmpty())
            recovery.barrier?.complete(Unit)
            advanceUntilIdle()
            assertEquals(details, viewModel.state.value.recoveredCreation)
            assertTrue(viewModel.state.value.canRetrySave)
            viewModel.create(details.copy(title = "Another trip"))
            viewModel.retrySave()
            advanceUntilIdle()
            assertEquals(listOf(pending), plans.requests)
            assertEquals("created-plan", viewModel.state.value.createdPlanId)
        }

    @Test
    fun creationRetryAfterAnUncertainNetworkResultKeepsItsRequestId() =
        runTest {
            val plans = CreationPlans().apply { failNext = true }
            val viewModel = createTravelPlannerTestViewModel(plans, CreationRecovery())
            viewModel.recoverCreation()
            viewModel.create(details)
            advanceUntilIdle()
            assertTrue(viewModel.state.value.canRetrySave)
            viewModel.retrySave()
            advanceUntilIdle()
            assertEquals(2, plans.requests.size)
            assertEquals(plans.requests.first(), plans.requests.last())
            assertEquals("created-plan", viewModel.state.value.createdPlanId)
        }

    @Test
    fun unreadableRecoveryStorageDoesNotAllowADuplicateCreation() =
        runTest {
            val plans = CreationPlans()
            val recovery = CreationRecovery().apply { failLoad = true }
            val viewModel = createTravelPlannerTestViewModel(plans, recovery)
            viewModel.recoverCreation()
            viewModel.create(details)
            advanceUntilIdle()
            assertTrue(viewModel.state.value.creationRecoveryFailed)
            assertFalse(viewModel.state.value.creationReady)
            assertTrue(plans.requests.isEmpty())
            recovery.failLoad = false
            viewModel.recoverCreation()
            viewModel.create(details)
            advanceUntilIdle()
            assertEquals(1, plans.requests.size)
        }

    @Test
    fun repeatedCreationTapsSubmitOnlyOneRequest() =
        runTest {
            val plans = CreationPlans()
            val viewModel = createTravelPlannerTestViewModel(plans, CreationRecovery())
            viewModel.recoverCreation()
            repeat(3) { viewModel.create(details) }
            advanceUntilIdle()
            viewModel.create(details)
            advanceUntilIdle()
            assertEquals(1, plans.requests.size)
        }
}

private class CreationPlans : TravelPlanRepository {
    override suspend fun recordActivity(
        planId: String,
        kind: com.bigong.oguri.domain.model.TravelUsageActivity,
    ) = Unit

    val requests: MutableList<TravelPendingSave.Create> = mutableListOf()
    var failNext: Boolean = false

    override suspend fun create(
        requestId: String,
        details: TravelPlanDetails,
    ): TravelPlanSummary {
        requests.add(TravelPendingSave.Create(requestId, details))
        if (failNext) {
            failNext = false
            throw TravelPlanException(TravelPlanFailure.CONNECTION)
        }
        return TravelPlanSummary("created-plan", details)
    }

    override suspend fun list(): List<TravelPlanSummary> = error("Unused")

    override suspend fun resume(): TravelPlanSummary? = error("Unused")

    override suspend fun recordView(
        planId: String,
        dayNumber: Int,
    ): Unit = error("Unused")

    override suspend fun get(planId: String): TravelPlan = error("Unused")

    override suspend fun changeVisit(
        planId: String,
        requestId: String,
        change: TravelVisitChange,
    ): TravelPlan = error("Unused")
}

private class CreationRecovery(
    var pending: TravelPendingSave? = null,
) : TravelRecoveryRepository {
    var barrier: CompletableDeferred<Unit>? = null
    var failLoad: Boolean = false

    override suspend fun load(planId: String?): TravelRecovery {
        barrier?.await()
        check(!failLoad)
        return TravelRecovery(pending = pending)
    }

    override suspend fun saveRequest(
        planId: String?,
        request: TravelPendingSave,
        draft: TravelDraft?,
    ) {
        pending = request
    }

    override suspend fun finishRequest(
        planId: String?,
        clearDraft: Boolean,
    ) {
        pending = null
    }

    override suspend fun rememberPlan(plan: TravelPlan): Unit = error("Unused")

    override suspend fun saveDraft(
        planId: String,
        draft: TravelDraft?,
    ): Unit = error("Unused")

    override suspend fun forget(planId: String): Unit = error("Unused")
}
