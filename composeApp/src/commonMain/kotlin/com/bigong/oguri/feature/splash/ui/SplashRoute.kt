package com.bigong.oguri.feature.splash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

private const val SPLASH_DELAY_MILLIS = 1200L

@Composable
fun SplashRoute(
    onSplashCompleted: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DELAY_MILLIS)
        onSplashCompleted()
    }

    SplashScreen()
}
