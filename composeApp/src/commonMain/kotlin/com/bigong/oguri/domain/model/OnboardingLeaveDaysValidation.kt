package com.bigong.oguri.domain.model

const val MAX_REMAINING_DAY_OFF: Int = 40
const val MIN_DAY_OFF: Int = 1

enum class OnboardingLeaveDaysValidationError {
    DAY_OFF_MUST_BE_POSITIVE,
    REMAINING_DAY_OFF_EXCEEDS_MAX,
    PREFERRED_DAY_OFF_EXCEEDS_REMAINING,
}

data class OnboardingLeaveDaysValidation(
    val remainingDayOffError: OnboardingLeaveDaysValidationError? = null,
    val preferredDayOffError: OnboardingLeaveDaysValidationError? = null,
) {
    val isValid: Boolean
        get() = remainingDayOffError == null && preferredDayOffError == null
}
