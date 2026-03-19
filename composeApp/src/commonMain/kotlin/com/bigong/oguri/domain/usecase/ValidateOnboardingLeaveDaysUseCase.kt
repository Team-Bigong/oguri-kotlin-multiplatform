package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.model.MAX_REMAINING_DAY_OFF
import com.bigong.oguri.domain.model.MIN_DAY_OFF
import com.bigong.oguri.domain.model.OnboardingLeaveDaysValidation
import com.bigong.oguri.domain.model.OnboardingLeaveDaysValidationError
import dev.zacsweers.metro.Inject

@Inject
class ValidateOnboardingLeaveDaysUseCase {
    operator fun invoke(
        remainingDayOff: Int?,
        preferredDayOff: Int?,
    ): OnboardingLeaveDaysValidation {
        val remainingDayOffError =
            when {
                remainingDayOff != null && remainingDayOff < MIN_DAY_OFF -> OnboardingLeaveDaysValidationError.DAY_OFF_MUST_BE_POSITIVE
                remainingDayOff != null && remainingDayOff > MAX_REMAINING_DAY_OFF -> OnboardingLeaveDaysValidationError.REMAINING_DAY_OFF_EXCEEDS_MAX
                else -> null
            }

        val preferredDayOffError =
            when {
                preferredDayOff != null && preferredDayOff < MIN_DAY_OFF -> OnboardingLeaveDaysValidationError.DAY_OFF_MUST_BE_POSITIVE
                remainingDayOff != null &&
                    preferredDayOff != null &&
                    preferredDayOff > remainingDayOff -> OnboardingLeaveDaysValidationError.PREFERRED_DAY_OFF_EXCEEDS_REMAINING
                else -> null
            }

        return OnboardingLeaveDaysValidation(
            remainingDayOffError = remainingDayOffError,
            preferredDayOffError = preferredDayOffError,
        )
    }
}
