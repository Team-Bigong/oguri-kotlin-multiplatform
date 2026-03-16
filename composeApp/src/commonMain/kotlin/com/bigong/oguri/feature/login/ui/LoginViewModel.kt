package com.bigong.oguri.feature.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.usecase.LoginWithKakaoAccessTokenUseCase
import com.bigong.oguri.feature.login.ui.model.LoginUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class LoginViewModel(
    private val loginWithKakaoAccessTokenUseCase: LoginWithKakaoAccessTokenUseCase,
) : ViewModel() {
    private val _uiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginWithKakaoAccessToken(kakaoAccessToken: String) {
        if (uiState.value.isLoading || kakaoAccessToken.isBlank()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { currentUiState: LoginUiState ->
                currentUiState.copy(isLoading = true)
            }
            runCatching {
                loginWithKakaoAccessTokenUseCase(kakaoAccessToken = kakaoAccessToken)
            }.onSuccess {
                _uiState.update { currentUiState: LoginUiState ->
                    currentUiState.copy(isLoginCompleted = true)
                }
            }.also {
                _uiState.update { currentUiState: LoginUiState ->
                    currentUiState.copy(isLoading = false)
                }
            }
        }
    }

    fun consumeLoginCompleted() {
        if (!uiState.value.isLoginCompleted) {
            return
        }
        _uiState.update { currentUiState: LoginUiState ->
            currentUiState.copy(isLoginCompleted = false)
        }
    }
}
