package com.bigong.oguri.feature.login.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bigong.oguri.domain.usecase.LoginWithKakaoAccessTokenUseCase
import com.bigong.oguri.feature.login.ui.model.LoginUiState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class LoginViewModel(
    private val loginWithKakaoAccessTokenUseCase: LoginWithKakaoAccessTokenUseCase,
) : ViewModel() {
    private val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var loginUiState: LoginUiState by mutableStateOf(LoginUiState())
        private set

    fun loginWithKakaoAccessToken(
        kakaoAccessToken: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ) {
        if (loginUiState.isLoading) {
            return
        }
        viewModelScope.launch {
            loginUiState = loginUiState.copy(isLoading = true)
            runCatching {
                withContext(Dispatchers.Default) {
                    loginWithKakaoAccessTokenUseCase(kakaoAccessToken = kakaoAccessToken)
                }
            }.onSuccess {
                onSuccess()
            }.onFailure {
                onFailure()
            }.also {
                loginUiState = loginUiState.copy(isLoading = false)
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
