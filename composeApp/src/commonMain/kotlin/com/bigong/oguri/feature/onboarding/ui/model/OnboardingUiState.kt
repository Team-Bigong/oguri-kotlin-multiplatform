package com.bigong.oguri.feature.onboarding.ui.model

import com.bigong.oguri.domain.model.OnboardingLeaveDaysValidationError

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.TERMS,
    val isServiceTermsChecked: Boolean = false,
    val isPrivacyPolicyChecked: Boolean = false,
    val remainingDayOffInput: String = "",
    val preferredDayOffInput: String = "",
    val isRemainingDayOffConfirmed: Boolean = false,
    val isPreferredDayOffConfirmed: Boolean = false,
    val remainingDayOffError: OnboardingLeaveDaysValidationError? = null,
    val preferredDayOffError: OnboardingLeaveDaysValidationError? = null,
)
