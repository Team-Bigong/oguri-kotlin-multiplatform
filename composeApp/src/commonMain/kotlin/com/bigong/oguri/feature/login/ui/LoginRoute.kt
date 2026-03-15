package com.bigong.oguri.feature.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.bigong.oguri.core.platform.loginWithKakao
import com.bigong.oguri.feature.login.ui.model.LoginSideEffect
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest
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

    LaunchedEffect(loginViewModel) {
        loginViewModel.sideEffect.collectLatest { sideEffect: LoginSideEffect ->
            when (sideEffect) {
                LoginSideEffect.NavigateToHome -> onKakaoLoginClick()
            }
        }
    }

    LoginScreen(
        onKakaoLoginClick = {
            coroutineScope.launch {
                val kakaoLoginResult: Result<String> = loginWithKakao()
                val kakaoAccessToken: String = kakaoLoginResult.getOrNull() ?: return@launch

                loginViewModel.loginWithKakaoAccessToken(kakaoAccessToken = kakaoAccessToken)
            }
        },
        onAppleLoginClick = onAppleLoginClick,
        onGuestBrowseClick = onGuestBrowseClick,
    )
}
