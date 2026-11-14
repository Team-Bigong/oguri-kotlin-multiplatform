package com.bigong.oguri.feature.travelplanner.ui

import com.bigong.oguri.domain.model.*
import com.bigong.oguri.domain.repository.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TravelManagementAccessTest {
    @BeforeTest fun prepare() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest fun reset() = Dispatchers.resetMain()

    @Test
    fun revokedMembershipClearsMembersInviteAndPreviouslyShownChanges() =
        runTest {
            val repository = ManagementCollaboration()
            val viewModel = TravelCollaborationViewModel(repository, ManagementPlans())
            viewModel.members("plan")
            advanceUntilIdle()
            viewModel.issue("plan")
            advanceUntilIdle()
            viewModel.recentChanges("plan")
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.members)
            assertNotNull(viewModel.state.value.invite)
            assertNotNull(viewModel.state.value.recentChanges)
            repository.failure = TravelPlanFailure.UNAVAILABLE
            viewModel.members("plan")
            advanceUntilIdle()
            assertNull(viewModel.state.value.members)
            assertNull(viewModel.state.value.invite)
            assertNull(viewModel.state.value.recentChanges)
            assertEquals(TravelPlanFailure.UNAVAILABLE, viewModel.state.value.failure)
        }

    @Test
    fun connectionFailurePreservesDetailsButExpiredAuthenticationClearsThem() =
        runTest {
            val repository = ManagementCollaboration()
            val viewModel = TravelCollaborationViewModel(repository, ManagementPlans())
            viewModel.loadDetails("plan")
            advanceUntilIdle()
            val original = assertNotNull(viewModel.state.value.detailsPlan)
            repository.failure = TravelPlanFailure.CONNECTION
            viewModel.members("plan")
            advanceUntilIdle()
            assertEquals(original, viewModel.state.value.detailsPlan)
            repository.failure = TravelPlanFailure.SIGN_IN_REQUIRED
            viewModel.members("plan")
            advanceUntilIdle()
            assertNull(viewModel.state.value.detailsPlan)
            assertEquals(TravelPlanFailure.SIGN_IN_REQUIRED, viewModel.state.value.failure)
        }

    @Test
    fun deniedAcknowledgementAlsoClearsTheManagementScreen() =
        runTest {
            val repository = ManagementCollaboration()
            val viewModel = TravelCollaborationViewModel(repository, ManagementPlans())
            viewModel.members("plan")
            advanceUntilIdle()
            repository.failure = TravelPlanFailure.UNAVAILABLE
            viewModel.dismissChanges("plan", 1)
            advanceUntilIdle()
            assertNull(viewModel.state.value.members)
            assertEquals(TravelPlanFailure.UNAVAILABLE, viewModel.state.value.failure)
        }

    @Test
    fun lateDetailsResponseCannotRestoreInformationAfterAccessEnds() =
        runTest {
            val repository = ManagementCollaboration()
            val plans = ManagementPlans().apply { barrier = CompletableDeferred() }
            val viewModel = TravelCollaborationViewModel(repository, plans)
            viewModel.members("plan")
            advanceUntilIdle()
            viewModel.loadDetails("plan")
            runCurrent()
            repository.failure = TravelPlanFailure.UNAVAILABLE
            viewModel.dismissChanges("plan", 1)
            runCurrent()
            assertNull(viewModel.state.value.members)
            plans.barrier?.complete(Unit)
            advanceUntilIdle()
            assertNull(viewModel.state.value.detailsPlan)
            assertEquals(TravelPlanFailure.UNAVAILABLE, viewModel.state.value.failure)
            assertFalse(viewModel.state.value.busy)
        }

    @Test
    fun cancelledDetailsFailureCannotReplaceTheAccessEndedState() =
        runTest {
            val repository = ManagementCollaboration()
            val plans =
                ManagementPlans().apply {
                    barrier = CompletableDeferred()
                    failureAfterRead = TravelPlanFailure.CONNECTION
                }
            val viewModel = TravelCollaborationViewModel(repository, plans)
            viewModel.loadDetails("plan")
            runCurrent()
            repository.failure = TravelPlanFailure.UNAVAILABLE
            viewModel.dismissChanges("plan", 1)
            runCurrent()
            plans.barrier?.complete(Unit)
            advanceUntilIdle()
            assertEquals(TravelPlanFailure.UNAVAILABLE, viewModel.state.value.failure)
            assertNull(viewModel.state.value.detailsPlan)
        }

    @Test
    fun unavailableReminderSettingsDoNotKeepThePreviousReservationVisible() =
        runTest {
            val repository = ManagementReminders()
            val viewModel = TravelReminderViewModel(repository)
            viewModel.load("plan")
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.settings)
            repository.failure = TravelPlanFailure.CONNECTION
            viewModel.load("plan")
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.settings)
            repository.failure = TravelPlanFailure.UNAVAILABLE
            viewModel.load("plan")
            advanceUntilIdle()
            assertNull(viewModel.state.value.settings)
            assertEquals(TravelPlanFailure.UNAVAILABLE, viewModel.state.value.failure)
        }
}

private class ManagementCollaboration : TravelCollaborationRepository {
    var failure: TravelPlanFailure? = null

    private fun checkAccess() {
        failure?.let { throw TravelPlanException(it) }
    }

    override suspend fun members(planId: String): TravelMembers {
        checkAccess()
        return TravelMembers("owner", listOf(TravelMember("owner", "Nickname", true)), null)
    }

    override suspend fun issue(planId: String): String = "secret-invite"

    override suspend fun recentChanges(planId: String): TravelRecentChanges =
        TravelRecentChanges(1, listOf(TravelChange(1, 1, "VISIT_ADDED")))

    override suspend fun markChangesSeen(
        planId: String,
        revision: Long,
    ) = checkAccess()

    override suspend fun revoke(planId: String): Unit = error("Unused")

    override suspend fun remove(
        planId: String,
        memberId: String,
    ): Unit = error("Unused")

    override suspend fun leave(planId: String): Unit = error("Unused")

    override suspend fun delete(planId: String): Unit = error("Unused")

    override suspend fun updateDetails(
        planId: String,
        details: TravelPlanDetails,
        revision: Long,
        unassignedCount: Int,
    ): Unit = error("Unused")

    override suspend fun offerOwnership(
        planId: String,
        targetId: String,
    ): Unit = error("Unused")

    override suspend fun acceptOwnership(planId: String): Unit = error("Unused")

    override suspend fun preview(token: String): TravelInvitePreview = error("Unused")

    override suspend fun join(token: String): String = error("Unused")
}

private class ManagementPlans : TravelPlanRepository {
    override suspend fun recordActivity(
        planId: String,
        kind: com.bigong.oguri.domain.model.TravelUsageActivity,
    ) = Unit

    var barrier: CompletableDeferred<Unit>? = null
    var failureAfterRead: TravelPlanFailure? = null

    override suspend fun get(planId: String): TravelPlan =
        withContext(NonCancellable) {
            barrier?.await()
            failureAfterRead?.let { throw TravelPlanException(it) }
            TravelPlan(
                TravelPlanSummary(
                    planId,
                    TravelPlanDetails(
                        "Seoul",
                        TimeZone.of("Asia/Seoul"),
                        "Private trip",
                        TravelDates(LocalDate(2027, 4, 1), LocalDate(2027, 4, 3)),
                    ),
                ),
                1,
                emptyMap(),
                emptyList(),
            )
        }

    override suspend fun list(): List<TravelPlanSummary> = error("Unused")

    override suspend fun resume(): TravelPlanSummary? = error("Unused")

    override suspend fun recordView(
        planId: String,
        dayNumber: Int,
    ): Unit = error("Unused")

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

private class ManagementReminders : TravelReminderRepository {
    var failure: TravelPlanFailure? = null

    override suspend fun settings(planId: String): TravelReminderSettings {
        failure?.let { throw TravelPlanException(it) }
        return TravelReminderSettings(
            TravelDates(LocalDate(2027, 4, 1), LocalDate(2027, 4, 3)),
            TimeZone.of("Asia/Seoul"),
            true,
            TravelReminder(
                "reminder",
                kotlin.time.Instant.parse("2027-03-31T10:00:00Z"),
                TimeZone.of("Asia/Seoul"),
                TravelReminderStatus.PENDING,
            ),
        )
    }

    override suspend fun syncDevice(): Unit = error("Unused")

    override suspend fun schedule(
        planId: String,
        settings: TravelReminderSettings,
        time: TravelReminderTime,
    ): TravelReminder = error("Unused")

    override suspend fun cancel(
        planId: String,
        reminderId: String,
    ): Unit = error("Unused")

    override suspend fun open(reminderId: String): String = error("Unused")
}
