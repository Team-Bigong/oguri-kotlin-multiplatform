package com.bigong.oguri.feature.login.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bigong.oguri.core.platform.loginWithApple
import com.bigong.oguri.core.platform.loginWithGoogle
import com.bigong.oguri.core.platform.loginWithKakao
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
import com.bigong.oguri.feature.login.ui.model.LoginSideEffect
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_login_failed
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginRoute(
    loginViewModelProvider: Provider<LoginViewModel>,
    snackbarHostState: SnackbarHostState,
    onLoginSucceeded: () -> Unit,
    onLoginCompleted: (Boolean) -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    val loginViewModel =
        remember {
            loginViewModelProvider()
        }
    val loginUiState = loginViewModel.uiState.collectAsStateWithLifecycle().value
    val coroutineScope = rememberCoroutineScope()
    val loginFailedMessage = stringResource(Res.string.snackbar_login_failed)

    LaunchedEffect(loginUiState.isOnboardingCompleted) {
        val isOnboardingCompleted = loginUiState.isOnboardingCompleted ?: return@LaunchedEffect
        onLoginSucceeded()
        onLoginCompleted(isOnboardingCompleted)
        loginViewModel.consumeLoginCompleted()
    }
    LaunchedEffect(loginViewModel) {
        loginViewModel.sideEffect.collectLatest { sideEffect ->
            when (sideEffect) {
                LoginSideEffect.LoginFailed -> {
                    snackbarHostState.showOguriSnackbar(
                        message = loginFailedMessage,
                        type = OguriSnackBarType.ALERT,
                    )
                }
            }
        }
    }

    LoginScreen(
        isLoading = loginUiState.isLoading,
        onGoogleLoginClick = {
            if (loginUiState.isLoading) {
                return@LoginScreen
            }
            coroutineScope.launch {
                val googleLoginResult = loginWithGoogle()
                val googleIdentityToken = googleLoginResult.getOrNull()?.trim().orEmpty()
                if (googleIdentityToken.isBlank()) {
                    loginViewModel.onLoginFailed()
                    return@launch
                }
                loginViewModel.loginWithGoogleIdentityToken(identityToken = googleIdentityToken)
            }
        },
        onKakaoLoginClick = {
            if (loginUiState.isLoading) {
                return@LoginScreen
            }
            coroutineScope.launch {
                val kakaoLoginResult = loginWithKakao()
                val kakaoAccessToken = kakaoLoginResult.getOrNull()?.trim().orEmpty()
                if (kakaoAccessToken.isBlank()) {
                    loginViewModel.onLoginFailed()
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
                val appleLoginResult = loginWithApple()
                val appleToken = appleLoginResult.getOrNull()?.trim().orEmpty()
                if (appleToken.isBlank()) {
                    loginViewModel.onLoginFailed()
                    return@launch
                }
                loginViewModel.loginWithAppleIdentityToken(identityToken = appleToken)
            }
        },
        onGuestBrowseClick = onGuestBrowseClick,
    )
}
