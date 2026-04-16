package com.bigong.oguri.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.ui.component.CenteredLoadingIndicator
import com.bigong.oguri.core.util.extension.dismissKeyboardOnOutsideTouch
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.feature.onboarding.ui.component.OnboardingLeaveDaysContent
import com.bigong.oguri.feature.onboarding.ui.component.OnboardingTermsContent
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingStep
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_back
import org.jetbrains.compose.resources.painterResource

private const val ONBOARDING_SCREEN_ANIMATION_DURATION_MILLIS = 260

@Composable
fun OnboardingScreen(
    onboardingUiState: OnboardingUiState,
    onBackClick: () -> Unit,
    onServiceTermsToggle: () -> Unit,
    onPrivacyPolicyToggle: () -> Unit,
    onServiceTermsOpen: () -> Unit,
    onPrivacyPolicyOpen: () -> Unit,
    onAgreeAllClick: () -> Unit,
    onRemainingDayOffChange: (String) -> Unit,
    onRemainingDayOffCommit: () -> Unit,
    onPreferredDayOffChange: (String) -> Unit,
    onPreferredDayOffCommit: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    PlatformBackHandler(
        enabled = true,
        onBack = onBackClick,
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .dismissKeyboardOnOutsideTouch(),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.btn_back),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Neutral90),
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .noRippleClickable(onClick = onBackClick),
                )
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedContent(
                targetState = onboardingUiState.step,
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = {
                    val isForward = targetState.ordinal > initialState.ordinal
                    (
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = ONBOARDING_SCREEN_ANIMATION_DURATION_MILLIS),
                            initialOffsetX = { fullWidth -> if (isForward) fullWidth else -fullWidth },
                        ) + fadeIn(animationSpec = tween(durationMillis = ONBOARDING_SCREEN_ANIMATION_DURATION_MILLIS))
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = ONBOARDING_SCREEN_ANIMATION_DURATION_MILLIS),
                            targetOffsetX = { fullWidth -> if (isForward) -fullWidth / 3 else fullWidth / 3 },
                        ) + fadeOut(animationSpec = tween(durationMillis = ONBOARDING_SCREEN_ANIMATION_DURATION_MILLIS)),
                    )
                },
                label = "onboarding_step_content",
            ) { onboardingStep ->
                when (onboardingStep) {
                    OnboardingStep.TERMS -> {
                        OnboardingTermsContent(
                            isServiceTermsChecked = onboardingUiState.isServiceTermsChecked,
                            isPrivacyPolicyChecked = onboardingUiState.isPrivacyPolicyChecked,
                            onServiceTermsToggle = onServiceTermsToggle,
                            onPrivacyPolicyToggle = onPrivacyPolicyToggle,
                            onServiceTermsOpen = onServiceTermsOpen,
                            onPrivacyPolicyOpen = onPrivacyPolicyOpen,
                            onAgreeAllClick = onAgreeAllClick,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }

                    OnboardingStep.LEAVE_DAYS -> {
                        OnboardingLeaveDaysContent(
                            remainingDayOffInput = onboardingUiState.remainingDayOffInput,
                            preferredDayOffInput = onboardingUiState.preferredDayOffInput,
                            remainingDayOffError = onboardingUiState.remainingDayOffError,
                            preferredDayOffError = onboardingUiState.preferredDayOffError,
                            onRemainingDayOffChange = onRemainingDayOffChange,
                            onRemainingDayOffCommit = onRemainingDayOffCommit,
                            onPreferredDayOffChange = onPreferredDayOffChange,
                            onPreferredDayOffCommit = onPreferredDayOffCommit,
                            isRemainingDayOffConfirmed = onboardingUiState.isRemainingDayOffConfirmed,
                            isPreferredDayOffConfirmed = onboardingUiState.isPreferredDayOffConfirmed,
                            isSubmitting = onboardingUiState.isSubmitting,
                            onCompleteClick = onCompleteClick,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }
        }
        if (onboardingUiState.isSubmitting) {
            CenteredLoadingIndicator()
        }
    }
}
