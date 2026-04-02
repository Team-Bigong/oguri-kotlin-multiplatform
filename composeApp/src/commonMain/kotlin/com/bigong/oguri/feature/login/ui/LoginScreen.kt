package com.bigong.oguri.feature.login.ui

import androidx.compose.runtime.Composable

@Composable
expect fun LoginScreen(
    isLoading: Boolean,
    onGoogleLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onGuestBrowseClick: () -> Unit,
)
