package com.bigong.oguri.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _sideEffect = MutableSharedFlow<LoginSideEffect>(extraBufferCapacity = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    fun loginWithKakaoAccessToken(kakaoAccessToken: String) {
        if (uiState.value.isLoading || kakaoAccessToken.isBlank()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { currentUiState ->
                currentUiState.copy(isLoading = true)
            }
            runCatching {
                loginWithKakaoAccessTokenUseCase(kakaoAccessToken = kakaoAccessToken)
            }.onSuccess {
                _uiState.update { currentUiState ->
                    currentUiState.copy(isLoginCompleted = true)
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
        if (!uiState.value.isLoginCompleted) {
            return
        }
        _uiState.update { currentUiState ->
            currentUiState.copy(isLoginCompleted = false)
        }
    }
}
