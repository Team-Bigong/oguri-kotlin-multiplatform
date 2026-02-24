package com.bigong.oguri.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bigong.oguri.data.repository.UserStateRepository
import com.bigong.oguri.feature.common.ui.rememberRouteViewModel
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingUiState

@Composable
fun OnboardingRoute(
    userStateRepository: UserStateRepository,
    onCalculateStrategyClick: () -> Unit,
) {
    val onboardingViewModel: OnboardingViewModel = rememberRouteViewModel(userStateRepository) {
        OnboardingViewModel(userStateRepository = userStateRepository)
    }
    val onboardingUiState: OnboardingUiState by onboardingViewModel.onboardingUiStateFlow.collectAsState()

    OnboardingScreen(
        onboardingUiState = onboardingUiState,
        onSelectUserRoleType = onboardingViewModel::selectUserRoleType,
        onMinusAnnualLeave = { onboardingViewModel.decreaseAnnualLeaveDays(onboardingUiState.remainingAnnualLeaveDays) },
        onPlusAnnualLeave = { onboardingViewModel.increaseAnnualLeaveDays(onboardingUiState.remainingAnnualLeaveDays) },
        onSelectWorkScheduleType = onboardingViewModel::selectWorkScheduleType,
        onCalculateStrategyClick = {
            onboardingViewModel.completeOnboarding()
            onCalculateStrategyClick()
        },
    )
}
