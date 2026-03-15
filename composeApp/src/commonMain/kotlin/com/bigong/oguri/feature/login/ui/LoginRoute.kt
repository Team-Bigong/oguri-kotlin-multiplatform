package com.bigong.oguri.feature.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.bigong.oguri.core.platform.loginWithKakao
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    loginViewModelProvider: Provider<LoginViewModel>,
    onKakaoLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    val loginViewModel: LoginViewModel = remember {
        loginViewModelProvider()
    }
    val coroutineScope = rememberCoroutineScope()

    LoginScreen(
        onKakaoLoginClick = {
            coroutineScope.launch {
                val loginResult = loginWithKakao()
                loginResult.getOrNull()?.let { accessToken: String ->
                    if (accessToken.isNotBlank()) {
                        loginViewModel.loginWithKakaoAccessToken(
                            kakaoAccessToken = accessToken,
                            onSuccess = onKakaoLoginClick,
                            onFailure = onKakaoLoginClick,
                        )
                        return@launch
                    }
                }
            }
        },
        onAppleLoginClick = onAppleLoginClick,
        onGuestBrowseClick = onGuestBrowseClick,
    )
}
