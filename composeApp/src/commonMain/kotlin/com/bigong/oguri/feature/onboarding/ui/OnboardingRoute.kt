package com.bigong.oguri.feature.onboarding.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.withResumed
import com.bigong.oguri.core.navigation.WebDocumentType
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingSideEffect
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingStep
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_onboarding_failed
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingRoute(
    onboardingViewModelProvider: Provider<OnboardingViewModel>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onCompleted: () -> Unit,
    onOpenWebDocument: (WebDocumentType) -> Unit,
) {
    val onboardingViewModel: OnboardingViewModel = viewModel { onboardingViewModelProvider() }
    val onboardingUiState by onboardingViewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnCompleted by rememberUpdatedState(onCompleted)
    val onboardingFailedMessage = stringResource(Res.string.snackbar_onboarding_failed)

    LaunchedEffect(onboardingUiState.isCompleted, lifecycle) {
        if (onboardingUiState.isCompleted) {
            lifecycle.withResumed { currentOnCompleted() }
        }
    }

    LaunchedEffect(onboardingViewModel) {
        onboardingViewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                OnboardingSideEffect.OnboardingFailed -> {
                    snackbarHostState.showOguriSnackbar(
                        message = onboardingFailedMessage,
                        type = OguriSnackBarType.ALERT,
                    )
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
        onSkipClick = onboardingViewModel::skipOnboardingLeaveDays,
        onCompleteClick = onboardingViewModel::completeOnboarding,
    )
}
