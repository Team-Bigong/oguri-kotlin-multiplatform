package com.bigong.oguri.feature.login.ui

import androidx.compose.runtime.Composable

@Composable
fun LoginRoute(
    onKakaoLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    LoginScreen(
        onKakaoLoginClick = onKakaoLoginClick,
        onAppleLoginClick = onAppleLoginClick,
        onGuestBrowseClick = onGuestBrowseClick,
    )
}
