package com.bigong.oguri.feature.onboarding.ui.model

import com.bigong.oguri.domain.model.OnboardingLeaveDaysValidationError
import com.bigong.oguri.domain.usecase.ValidateOnboardingLeaveDaysUseCase.Companion.DEFAULT_PREFERRED_DAY_OFF

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.TERMS,
    val isServiceTermsChecked: Boolean = false,
    val isPrivacyPolicyChecked: Boolean = false,
    val remainingDayOffInput: String = "",
    val preferredDayOffInput: String = DEFAULT_PREFERRED_DAY_OFF.toString(),
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
