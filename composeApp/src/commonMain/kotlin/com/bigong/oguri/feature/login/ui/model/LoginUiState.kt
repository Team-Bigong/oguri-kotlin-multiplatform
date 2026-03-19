package com.bigong.oguri.feature.login.ui.model

data class LoginUiState(
    val isLoading: Boolean = false,
    val isOnboardingCompleted: Boolean? = null,
)
