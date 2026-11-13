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
    internal val state: StateFlow<TravelCollaborationState> = mutableState.asStateFlow()

    internal fun members(planId: String) =
        perform {
            val members = repository.members(planId)
            mutableState.update { it.copy(members = members) }
        }

    internal fun issue(planId: String) =
        perform {
            val token = repository.issue(planId)
            mutableState.update { it.copy(invite = token) }
        }

    internal fun revoke(planId: String) =
        perform {
            repository.revoke(planId)
            mutableState.update { it.copy(invite = null) }
        }

    internal fun remove(
        planId: String,
        memberId: String,
    ) = perform {
        repository.remove(planId, memberId)
        val members = repository.members(planId)
        mutableState.update { it.copy(invite = null, members = members) }
    }

    internal fun leave(planId: String) =
        perform {
            repository.leave(planId)
            mutableState.update { it.copy(closed = true) }
        }

    internal fun delete(planId: String) =
        perform {
            repository.delete(planId)
            mutableState.update { it.copy(closed = true) }
        }

    internal fun offerOwnership(
        planId: String,
        targetId: String,
    ) = perform {
        repository.offerOwnership(planId, targetId)
        val members = repository.members(planId)
        mutableState.update { it.copy(members = members) }
    }

    internal fun acceptOwnership(planId: String) =
        perform {
            repository.acceptOwnership(planId)
            val members = repository.members(planId)
            mutableState.update { it.copy(members = members) }
        }

    internal fun loadDetails(planId: String) =
        perform {
            val plan = plans.get(planId)
            mutableState.update { it.copy(detailsPlan = plan) }
        }

    internal fun updateDetails(
        plan: TravelPlan,
        details: TravelPlanDetails,
        affected: Int,
    ) = perform {
        repository.updateDetails(plan.summary.id, details, plan.revision, affected)
        mutableState.update { it.copy(detailsPlan = null) }
    }

    internal fun dismissDetails() {
        if (!state.value.busy) mutableState.update { it.copy(detailsPlan = null, failure = null) }
    }

    internal fun recentChanges(planId: String) =
        perform {
            val changes = repository.recentChanges(planId)
            mutableState.update { it.copy(recentChanges = changes) }
        }

    internal fun dismissChanges(
        planId: String,
        shownRevision: Long,
    ) {
        mutableState.update { it.copy(recentChanges = null) }
        viewModelScope.launch {
            try {
                repository.markChangesSeen(planId, shownRevision)
            } catch (_: TravelPlanException) {
                // The same changes can be shown again when this acknowledgement fails.
            }
        }
    }

    internal fun preview(token: String) {
        inviteJob?.cancel()
        mutableState.value = TravelCollaborationState(busy = true)
        inviteJob =
            viewModelScope.launch {
                try {
                    val preview = repository.preview(token)
                    mutableState.update { it.copy(preview = preview, busy = false) }
                } catch (exception: TravelPlanException) {
                    mutableState.update { it.copy(failure = exception.failure, busy = false) }
                }
            }
    }

    internal fun join(token: String) =
        perform {
            val joined = repository.join(token)
            mutableState.update { it.copy(joinedPlanId = joined) }
        }

    private fun perform(action: suspend () -> Unit) {
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true, failure = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (exception: TravelPlanException) {
                mutableState.update { it.copy(failure = exception.failure) }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
