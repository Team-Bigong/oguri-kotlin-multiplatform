package com.bigong.oguri.feature.travelplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.core.notification.requestTravelReminderPermission
import com.bigong.oguri.domain.model.TravelPlanException
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelReminderTime
import com.bigong.oguri.domain.repository.TravelReminderRepository
import com.bigong.oguri.feature.travelplanner.ui.model.TravelReminderUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class TravelReminderViewModel(
    private val repository: TravelReminderRepository,
) : ViewModel() {
    private val mutableState: MutableStateFlow<TravelReminderUiState> = MutableStateFlow(TravelReminderUiState())
    private var openingJob: Job? = null
    internal val state: StateFlow<TravelReminderUiState> = mutableState.asStateFlow()

    internal fun load(planId: String) =
        perform {
            val settings = repository.settings(planId)
            val permissionDenied = com.bigong.oguri.core.notification.travelReminderPermissionDenied()
            mutableState.update { it.copy(settings = settings, permissionDenied = permissionDenied) }
        }

    internal fun schedule(
        planId: String,
        time: TravelReminderTime,
    ) {
        val settings = state.value.settings ?: return
        if (!settings.canSchedule) return
        perform {
            if (!requestTravelReminderPermission()) {
                mutableState.update { it.copy(permissionDenied = true) }
                return@perform
            }
            repository.schedule(planId, settings, time)
            mutableState.update { it.copy(settings = repository.settings(planId)) }
        }
    }

    internal suspend fun refreshPermission() {
        val denied = com.bigong.oguri.core.notification.travelReminderPermissionDenied()
        mutableState.update { it.copy(permissionDenied = denied) }
    }

    internal fun cancel(planId: String) {
        val reminder = state.value.settings?.reminder ?: return
        perform {
            repository.cancel(planId, reminder.id)
            mutableState.update { it.copy(settings = repository.settings(planId)) }
        }
    }

    internal fun open(reminderId: String) {
        openingJob?.cancel()
        mutableState.update { it.copy(busy = true, failure = null, openedPlanId = null, openedReminderId = null) }
        openingJob =
            viewModelScope.launch {
                try {
                    val planId = repository.open(reminderId)
                    mutableState.update { it.copy(busy = false, openedPlanId = planId, openedReminderId = reminderId) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: TravelPlanException) {
                    mutableState.update { it.copy(busy = false, failure = failure.failure) }
                } catch (_: Exception) {
                    mutableState.update { it.copy(busy = false, failure = TravelPlanFailure.CONNECTION) }
                }
            }
    }

    internal fun clearOpening() {
        openingJob?.cancel()
        mutableState.update { it.copy(busy = false, openedPlanId = null, openedReminderId = null, failure = null) }
    }

    internal suspend fun syncDevice() {
        try {
            repository.syncDevice()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Retained revocations are retried on the next foreground or token change.
        }
    }

    private fun perform(action: suspend () -> Unit) {
        if (state.value.busy) return
        mutableState.update { it.copy(busy = true, failure = null, permissionDenied = false) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: TravelPlanException) {
                mutableState.update {
                    if (failure.failure == TravelPlanFailure.UNAVAILABLE || failure.failure == TravelPlanFailure.SIGN_IN_REQUIRED) {
                        TravelReminderUiState(failure = failure.failure)
                    } else {
                        it.copy(failure = failure.failure)
                    }
                }
            } catch (_: Exception) {
                mutableState.update { it.copy(failure = TravelPlanFailure.CONNECTION) }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
