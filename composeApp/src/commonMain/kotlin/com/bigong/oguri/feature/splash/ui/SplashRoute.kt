package com.bigong.oguri.feature.splash.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.delay

private const val SPLASH_DELAY_MILLIS = 1200L

@Composable
fun SplashRoute(
    splashViewModelProvider: Provider<SplashViewModel>,
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val splashViewModel =
        remember {
            splashViewModelProvider()
        }
    val destination by splashViewModel.destinationState.collectAsStateWithLifecycle()

    LaunchedEffect(splashViewModel) {
        splashViewModel.resolveDestination()
    }

    LaunchedEffect(destination) {
        val targetDestination = destination ?: return@LaunchedEffect
        delay(SPLASH_DELAY_MILLIS)
        when (targetDestination) {
            SplashDestination.Login -> onNavigateToLogin()
            SplashDestination.Onboarding -> onNavigateToOnboarding()
            SplashDestination.Home -> onNavigateToHome()
        }
    }

    SplashScreen()
}
