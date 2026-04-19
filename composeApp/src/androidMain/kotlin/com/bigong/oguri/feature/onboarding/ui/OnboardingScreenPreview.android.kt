package com.bigong.oguri.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingStep
import com.bigong.oguri.feature.onboarding.ui.model.OnboardingUiState

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OnboardingScreenTermsPreview() {
    OguriTheme {
        OnboardingScreen(
            onboardingUiState = OnboardingUiState(step = OnboardingStep.TERMS),
            onBackClick = {},
            onServiceTermsToggle = {},
            onPrivacyPolicyToggle = {},
            onServiceTermsOpen = {},
            onPrivacyPolicyOpen = {},
            onAgreeAllClick = {},
            onRemainingDayOffChange = {},
            onRemainingDayOffCommit = {},
            onPreferredDayOffChange = {},
            onPreferredDayOffCommit = {},
            onSkipClick = {},
            onCompleteClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OnboardingScreenLeaveDaysPreview() {
    OguriTheme {
        OnboardingScreen(
            onboardingUiState =
                OnboardingUiState(
                    step = OnboardingStep.LEAVE_DAYS,
                    remainingDayOffInput = "21",
                    preferredDayOffInput = "3",
                    isRemainingDayOffConfirmed = true,
                    isPreferredDayOffConfirmed = true,
                ),
            onBackClick = {},
            onServiceTermsToggle = {},
            onPrivacyPolicyToggle = {},
            onServiceTermsOpen = {},
            onPrivacyPolicyOpen = {},
            onAgreeAllClick = {},
            onRemainingDayOffChange = {},
            onRemainingDayOffCommit = {},
            onPreferredDayOffChange = {},
            onPreferredDayOffCommit = {},
            onSkipClick = {},
            onCompleteClick = {},
        )
    }
}
