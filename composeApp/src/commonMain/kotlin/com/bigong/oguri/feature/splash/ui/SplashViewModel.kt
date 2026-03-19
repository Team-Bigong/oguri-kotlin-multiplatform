package com.bigong.oguri.feature.splash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.usecase.GetAutoLoginStateUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class SplashViewModel(
    private val getAutoLoginStateUseCase: GetAutoLoginStateUseCase,
) : ViewModel() {
    private val _destinationState = MutableStateFlow<SplashDestination?>(null)
    val destinationState = _destinationState.asStateFlow()

    fun resolveDestination() {
        if (_destinationState.value != null) {
            return
        }
        viewModelScope.launch {
            val autoLoginState = getAutoLoginStateUseCase()
            _destinationState.update {
                when {
                    !autoLoginState.isLoggedIn -> SplashDestination.Login
                    autoLoginState.isOnboardingCompleted -> SplashDestination.Home
                    else -> SplashDestination.Onboarding
                }
            }
        }
    }
}

enum class SplashDestination {
    Login,
    Onboarding,
    Home,
}
