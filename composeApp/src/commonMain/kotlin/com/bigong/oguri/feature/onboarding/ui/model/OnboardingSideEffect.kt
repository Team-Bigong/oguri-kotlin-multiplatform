package com.bigong.oguri.feature.onboarding.ui.model

import com.bigong.oguri.core.navigation.WebDocumentType

sealed interface OnboardingSideEffect {
    data object NavigateToHome : OnboardingSideEffect

    data class OpenWebDocument(
        val documentType: WebDocumentType,
    ) : OnboardingSideEffect
}
