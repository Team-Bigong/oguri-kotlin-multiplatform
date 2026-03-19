package com.bigong.oguri.feature.onboarding.ui.model

import com.bigong.oguri.core.navigation.WebDocumentType

sealed interface OnboardingSideEffect {
    data object OnboardingCompleted : OnboardingSideEffect

    data object OnboardingFailed : OnboardingSideEffect

    data class OpenWebDocument(
        val documentType: WebDocumentType,
    ) : OnboardingSideEffect
}
