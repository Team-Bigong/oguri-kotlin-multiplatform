package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OnboardingLeaveDaysContentWelcomePreview() {
    OguriTheme {
        OnboardingLeaveDaysContent(
            remainingDayOffInput = "21",
            preferredDayOffInput = "",
            remainingDayOffError = null,
            preferredDayOffError = null,
            onRemainingDayOffChange = {},
            onRemainingDayOffCommit = {},
            onPreferredDayOffChange = {},
            onPreferredDayOffCommit = {},
            isRemainingDayOffConfirmed = false,
            isPreferredDayOffConfirmed = false,
            isSubmitting = false,
            onCompleteClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OnboardingLeaveDaysContentAlmostPreview() {
    OguriTheme {
        OnboardingLeaveDaysContent(
            remainingDayOffInput = "21",
            preferredDayOffInput = "3",
            remainingDayOffError = null,
            preferredDayOffError = null,
            onRemainingDayOffChange = {},
            onRemainingDayOffCommit = {},
            onPreferredDayOffChange = {},
            onPreferredDayOffCommit = {},
            isRemainingDayOffConfirmed = true,
            isPreferredDayOffConfirmed = false,
            isSubmitting = false,
            onCompleteClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OnboardingLeaveDaysContentDonePreview() {
    OguriTheme {
        OnboardingLeaveDaysContent(
            remainingDayOffInput = "21",
            preferredDayOffInput = "3",
            remainingDayOffError = null,
            preferredDayOffError = null,
            onRemainingDayOffChange = {},
            onRemainingDayOffCommit = {},
            onPreferredDayOffChange = {},
            onPreferredDayOffCommit = {},
            isRemainingDayOffConfirmed = true,
            isPreferredDayOffConfirmed = true,
            isSubmitting = false,
            onCompleteClick = {},
        )
    }
}
