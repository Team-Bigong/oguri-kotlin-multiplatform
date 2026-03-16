package com.bigong.oguri.feature.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.platform.loginWithApple
import com.bigong.oguri.core.platform.loginWithKakao
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    loginViewModelProvider: Provider<LoginViewModel>,
    onLoginCompleted: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    val loginViewModel: LoginViewModel = remember {
        loginViewModelProvider()
    }
    val loginUiState = loginViewModel.uiState.collectAsStateWithLifecycle().value
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(loginUiState.isLoginCompleted) {
        if (loginUiState.isLoginCompleted) {
            onLoginCompleted()
            loginViewModel.consumeLoginCompleted()
        }
    }

    LoginScreen(
        onKakaoLoginClick = {
            if (loginUiState.isLoading) {
                return@LoginScreen
            }
            coroutineScope.launch {
                val kakaoLoginResult: Result<String> = loginWithKakao()
                val kakaoAccessToken: String = kakaoLoginResult.getOrNull()?.trim().orEmpty()
                if (kakaoAccessToken.isBlank()) {
                    return@launch
                }
                loginViewModel.loginWithKakaoAccessToken(kakaoAccessToken = kakaoAccessToken)
            }
        },
        onAppleLoginClick = {
            if (loginUiState.isLoading) {
                return@LoginScreen
            }
            coroutineScope.launch {
                val appleLoginResult: Result<String> = loginWithApple()
                val appleToken: String = appleLoginResult.getOrNull()?.trim().orEmpty()
                if (appleToken.isBlank()) {
                    return@launch
                }
                onAppleLoginClick()
            }
        },
        onGuestBrowseClick = onGuestBrowseClick,
    )
}
