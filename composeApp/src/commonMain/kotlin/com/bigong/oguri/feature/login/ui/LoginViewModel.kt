package com.bigong.oguri.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.usecase.LoginWithAppleIdentityTokenUseCase
import com.bigong.oguri.domain.usecase.LoginWithGoogleIdentityTokenUseCase
import com.bigong.oguri.domain.usecase.LoginWithKakaoAccessTokenUseCase
import com.bigong.oguri.feature.login.ui.model.LoginSideEffect
import com.bigong.oguri.feature.login.ui.model.LoginUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class LoginViewModel(
    private val loginWithKakaoAccessTokenUseCase: LoginWithKakaoAccessTokenUseCase,
    private val loginWithGoogleIdentityTokenUseCase: LoginWithGoogleIdentityTokenUseCase,
    private val loginWithAppleIdentityTokenUseCase: LoginWithAppleIdentityTokenUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<LoginSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    fun loginWithKakaoAccessToken(kakaoAccessToken: String) {
        if (uiState.value.isLoading || kakaoAccessToken.isBlank()) {
            return
        }

        login(
            loginRequest = {
                loginWithKakaoAccessTokenUseCase(kakaoAccessToken = kakaoAccessToken)
            },
        )
    }

    fun loginWithAppleIdentityToken(identityToken: String) {
        if (uiState.value.isLoading || identityToken.isBlank()) {
            return
        }

        login(
            loginRequest = {
                loginWithAppleIdentityTokenUseCase(identityToken = identityToken)
            },
        )
    }

    fun loginWithGoogleIdentityToken(identityToken: String) {
        if (uiState.value.isLoading || identityToken.isBlank()) {
            return
        }

        login(
            loginRequest = {
                loginWithGoogleIdentityTokenUseCase(identityToken = identityToken)
            },
        )
    }

    private fun login(loginRequest: suspend () -> Boolean) {
        viewModelScope.launch {
            _uiState.update { currentUiState ->
                currentUiState.copy(isLoading = true)
            }
            runCatching {
                loginRequest()
            }.onSuccess { isOnboardingCompleted ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(isOnboardingCompleted = isOnboardingCompleted)
                }
            }.onFailure {
                _sideEffect.tryEmit(LoginSideEffect.LoginFailed)
            }.also {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isLoading = false)
                }
            }
        }
    }

    fun onLoginFailed() {
        _sideEffect.tryEmit(LoginSideEffect.LoginFailed)
    }

    fun consumeLoginCompleted() {
        if (uiState.value.isOnboardingCompleted == null) {
            return
        }
        _uiState.update { currentUiState ->
            currentUiState.copy(isOnboardingCompleted = null)
        }
    }
}
