package com.bigong.oguri.domain.usecase

import com.bigong.oguri.domain.repository.AuthRepository
import dev.zacsweers.metro.Inject

@Inject
class CompleteOnboardingUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        preferredDayOff: Int,
        remainingDayOff: Int,
    ) {
        authRepository.completeOnboarding(
            preferredDayOff = preferredDayOff,
            remainingDayOff = remainingDayOff,
        )
    }
}
