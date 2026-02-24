package com.bigong.oguri.feature.onboarding.ui

import com.bigong.oguri.data.model.UserState
import com.bigong.oguri.data.model.UserRoleType
import com.bigong.oguri.data.model.WorkScheduleType
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.RouteViewModel
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val ANNUAL_LEAVE_STEP: Int = 1

class OnboardingViewModel(
    private val userStateRepository: UserStateRepository,
) : RouteViewModel() {
    private val mutableOnboardingUiStateFlow: MutableStateFlow<OnboardingUiState> = MutableStateFlow(OnboardingUiState())
    val onboardingUiStateFlow: StateFlow<OnboardingUiState> = mutableOnboardingUiStateFlow.asStateFlow()

    init {
        routeViewModelScope.launch {
            userStateRepository.userStateFlow.collect { userState: UserState ->
                mutableOnboardingUiStateFlow.update {
                    OnboardingUiState(
                        userRoleType = userState.userRoleType,
                        remainingAnnualLeaveDays = userState.remainingAnnualLeaveDays,
                        workScheduleType = userState.workScheduleType,
                    )
                }
            }
        }
    }

    fun selectUserRoleType(userRoleType: UserRoleType) {
        userStateRepository.updateUserRoleType(userRoleType)
    }

    fun decreaseAnnualLeaveDays(currentDays: Int) {
        userStateRepository.updateRemainingAnnualLeaveDays(currentDays - ANNUAL_LEAVE_STEP)
    }

    fun increaseAnnualLeaveDays(currentDays: Int) {
        userStateRepository.updateRemainingAnnualLeaveDays(currentDays + ANNUAL_LEAVE_STEP)
    }

    fun selectWorkScheduleType(workScheduleType: WorkScheduleType) {
        userStateRepository.updateWorkScheduleType(workScheduleType)
    }

    fun completeOnboarding() {
        userStateRepository.completeOnboarding()
    }
}
