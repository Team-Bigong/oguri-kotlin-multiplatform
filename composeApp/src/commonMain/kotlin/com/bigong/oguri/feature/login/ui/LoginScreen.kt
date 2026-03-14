package com.bigong.oguri.feature.login.ui

import androidx.compose.runtime.Composable

@Composable
expect fun LoginScreen(
    onKakaoLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onGuestBrowseClick: () -> Unit,
)
