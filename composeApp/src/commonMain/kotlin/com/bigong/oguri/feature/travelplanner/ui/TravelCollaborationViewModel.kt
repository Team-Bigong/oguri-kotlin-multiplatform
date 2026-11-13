package com.bigong.oguri.feature.travelplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.model.TravelInvitePreview
import com.bigong.oguri.domain.model.TravelMembers
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanException
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelRecentChanges
import com.bigong.oguri.domain.repository.TravelCollaborationRepository
import com.bigong.oguri.domain.repository.TravelPlanRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class TravelCollaborationState(
    val members: TravelMembers? = null,
    val detailsPlan: TravelPlan? = null,
    val recentChanges: TravelRecentChanges? = null,
    val preview: TravelInvitePreview? = null,
    val invite: String? = null,
    val joinedPlanId: String? = null,
    val closed: Boolean = false,
    val busy: Boolean = false,
    val failure: TravelPlanFailure? = null,
)

@Inject
class TravelCollaborationViewModel(
    private val repository: TravelCollaborationRepository,
    private val plans: TravelPlanRepository,
) : ViewModel() {
    private val mutableState: MutableStateFlow<TravelCollaborationState> = MutableStateFlow(TravelCollaborationState())
    private var inviteJob: Job? = null
    private var operationJob: Job? = null
    private var operationGeneration: Long = 0
    internal val state: StateFlow<TravelCollaborationState> = mutableState.asStateFlow()

    internal fun members(planId: String) =
        perform {
            val members = checkedResult { repository.members(planId) }
            mutableState.update { it.copy(members = members) }
        }

    internal fun issue(planId: String) =
        perform {
            val token = checkedResult { repository.issue(planId) }
            mutableState.update { it.copy(invite = token) }
        }

    internal fun revoke(planId: String) =
        perform {
            checkedResult { repository.revoke(planId) }
            mutableState.update { it.copy(invite = null) }
        }

    internal fun remove(
        planId: String,
        memberId: String,
    ) = perform {
        checkedResult { repository.remove(planId, memberId) }
        val members = checkedResult { repository.members(planId) }
        mutableState.update { it.copy(invite = null, members = members) }
    }

    internal fun leave(planId: String) =
        perform {
            checkedResult { repository.leave(planId) }
            mutableState.update { it.copy(closed = true) }
        }

    internal fun delete(planId: String) =
        perform {
            checkedResult { repository.delete(planId) }
            mutableState.update { it.copy(closed = true) }
        }

    internal fun offerOwnership(
        planId: String,
        targetId: String,
    ) = perform {
        checkedResult { repository.offerOwnership(planId, targetId) }
        val members = checkedResult { repository.members(planId) }
        mutableState.update { it.copy(members = members) }
    }

    internal fun acceptOwnership(planId: String) =
        perform {
            checkedResult { repository.acceptOwnership(planId) }
            val members = checkedResult { repository.members(planId) }
            mutableState.update { it.copy(members = members) }
        }

    internal fun loadDetails(planId: String) =
        perform {
            val plan = checkedResult { plans.get(planId) }
            mutableState.update { it.copy(detailsPlan = plan) }
        }

    internal fun updateDetails(
        plan: TravelPlan,
        details: TravelPlanDetails,
        affected: Int,
    ) = perform {
        checkedResult { repository.updateDetails(plan.summary.id, details, plan.revision, affected) }
        mutableState.update { it.copy(detailsPlan = null) }
    }

    internal fun dismissDetails() {
        if (!state.value.busy) mutableState.update { it.copy(detailsPlan = null, failure = null) }
    }

    internal fun recentChanges(planId: String) =
        perform {
            val changes = checkedResult { repository.recentChanges(planId) }
            mutableState.update { it.copy(recentChanges = changes) }
        }

    internal fun dismissChanges(
        planId: String,
        shownRevision: Long,
    ) {
        mutableState.update { it.copy(recentChanges = null) }
        viewModelScope.launch {
            try {
                checkedResult { repository.markChangesSeen(planId, shownRevision) }
            } catch (exception: TravelPlanException) {
                if (exception.failure == TravelPlanFailure.UNAVAILABLE || exception.failure == TravelPlanFailure.SIGN_IN_REQUIRED) {
                    applyFailure(exception.failure)
                }
                // A connection failure leaves acknowledgement available for the next visit.
            }
        }
    }

    internal fun preview(token: String) {
        operationGeneration++
        operationJob?.cancel()
        inviteJob?.cancel()
        mutableState.value = TravelCollaborationState(busy = true)
        inviteJob =
            viewModelScope.launch {
                try {
                    val preview = checkedResult { repository.preview(token) }
                    mutableState.update { it.copy(preview = preview, busy = false) }
                } catch (exception: TravelPlanException) {
                    mutableState.update { it.copy(failure = exception.failure, busy = false) }
                }
            }
    }

    internal fun join(token: String) =
        perform {
            val joined = checkedResult { repository.join(token) }
            mutableState.update { it.copy(joinedPlanId = joined) }
        }

    internal fun applyAccessFailure(failure: TravelPlanFailure) {
        if (failure == TravelPlanFailure.UNAVAILABLE || failure == TravelPlanFailure.SIGN_IN_REQUIRED) applyFailure(failure)
    }

    private fun applyFailure(failure: TravelPlanFailure) {
        if (failure == TravelPlanFailure.UNAVAILABLE || failure == TravelPlanFailure.SIGN_IN_REQUIRED) {
            operationGeneration++
            operationJob?.cancel()
            inviteJob?.cancel()
        }
        mutableState.update {
            if (failure == TravelPlanFailure.UNAVAILABLE || failure == TravelPlanFailure.SIGN_IN_REQUIRED) {
                TravelCollaborationState(failure = failure)
            } else {
                it.copy(failure = failure)
            }
        }
    }

    private fun perform(action: suspend () -> Unit) {
        if (mutableState.value.busy) return
        val generation = ++operationGeneration
        mutableState.update { it.copy(busy = true, failure = null) }
        operationJob =
            viewModelScope.launch {
                try {
                    action()
                } catch (exception: TravelPlanException) {
                    applyFailure(exception.failure)
                } finally {
                    if (generation == operationGeneration) mutableState.update { it.copy(busy = false) }
                }
            }
    }

    private suspend fun <Result> checkedResult(block: suspend () -> Result): Result {
        try {
            return block()
        } finally {
            currentCoroutineContext().ensureActive()
        }
    }
}
