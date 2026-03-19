package com.bigong.oguri.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.navigation.WebDocumentType
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingSideEffect
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingStep
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingRoute(
    onboardingViewModelProvider: Provider<OnboardingViewModel>,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onOpenWebDocument: (WebDocumentType) -> Unit,
) {
    val onboardingViewModel =
        remember {
            onboardingViewModelProvider()
        }
    val onboardingUiState = onboardingViewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(onboardingViewModel) {
        onboardingViewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                OnboardingSideEffect.NavigateToHome -> {
                    onHomeClick()
                }

                is OnboardingSideEffect.OpenWebDocument -> {
                    onOpenWebDocument(sideEffect.documentType)
                }
            }
        }
    }

    OnboardingScreen(
        onboardingUiState = onboardingUiState,
        onBackClick = {
            if (onboardingUiState.step == OnboardingStep.LEAVE_DAYS) {
                onboardingViewModel.onBackFromLeaveDays()
            } else {
                onBackClick()
            }
        },
        onServiceTermsToggle = onboardingViewModel::toggleServiceTermsChecked,
        onPrivacyPolicyToggle = onboardingViewModel::togglePrivacyPolicyChecked,
        onServiceTermsOpen = onboardingViewModel::openTermsOfService,
        onPrivacyPolicyOpen = onboardingViewModel::openPrivacyPolicy,
        onAgreeAllClick = onboardingViewModel::agreeAllAndMoveToNext,
        onRemainingDayOffChange = onboardingViewModel::updateRemainingDayOffInput,
        onRemainingDayOffCommit = onboardingViewModel::commitRemainingDayOffInput,
        onPreferredDayOffChange = onboardingViewModel::updatePreferredDayOffInput,
        onPreferredDayOffCommit = onboardingViewModel::commitPreferredDayOffInput,
        onCompleteClick = onboardingViewModel::completeOnboarding,
    )
}
