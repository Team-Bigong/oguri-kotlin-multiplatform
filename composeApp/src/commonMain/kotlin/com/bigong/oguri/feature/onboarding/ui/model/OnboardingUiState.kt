package com.bigong.oguri.feature.onboarding.ui.model

import com.bigong.oguri.domain.model.OnboardingLeaveDaysValidationError

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.TERMS,
    val isServiceTermsChecked: Boolean = false,
    val isPrivacyPolicyChecked: Boolean = false,
    val remainingDayOffInput: String = "",
    val preferredDayOffInput: String = "",
    val remainingDayOffError: OnboardingLeaveDaysValidationError? = null,
    val preferredDayOffError: OnboardingLeaveDaysValidationError? = null,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
) {
    internal val shouldShowPreferredDayOffInput: Boolean
        get() = remainingDayOffInput.toIntOrNull() != null && remainingDayOffError == null

    internal val hasValidDayOffInputs: Boolean
        get() = shouldShowPreferredDayOffInput && preferredDayOffInput.toIntOrNull() != null && preferredDayOffError == null

    internal val canStartSubmission: Boolean
        get() = step == OnboardingStep.LEAVE_DAYS && !isSubmitting && !isCompleted
}
