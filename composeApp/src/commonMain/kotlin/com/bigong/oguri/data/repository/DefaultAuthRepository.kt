package com.bigong.oguri.data.repository

import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.data.remote.AuthRemoteDataSource
import com.bigong.oguri.data.remote.model.request.AppleLoginRequest
import com.bigong.oguri.data.remote.model.request.GoogleLoginRequest
import com.bigong.oguri.data.remote.model.request.KakaoLoginRequest
import com.bigong.oguri.data.remote.model.request.RefreshTokenRequest
import com.bigong.oguri.data.remote.model.request.UpdateMemberDayOffRequest
import com.bigong.oguri.domain.model.AutoLoginState
import com.bigong.oguri.domain.repository.AuthRepository
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

@Inject
class DefaultAuthRepository(
    private val authRemoteDataSource: AuthRemoteDataSource,
) : AuthRepository {
    override suspend fun loginWithKakaoAccessToken(kakaoAccessToken: String): Boolean {
        require(value = kakaoAccessToken.isNotBlank()) {
            "Kakao access token is empty."
        }
        val loginResponse =
            authRemoteDataSource.loginWithKakao(
                request = KakaoLoginRequest(accessToken = kakaoAccessToken),
            )
        saveTokens(
            accessToken = loginResponse.accessToken,
            refreshToken = loginResponse.refreshToken,
        )
        return loginResponse.onboardingCompleted
    }

    override suspend fun loginWithGoogleIdentityToken(identityToken: String): Boolean {
        require(value = identityToken.isNotBlank()) {
            "Google identity token is empty."
        }
        val loginResponse =
            authRemoteDataSource.loginWithGoogle(
                request = GoogleLoginRequest(identityToken = identityToken),
            )
        saveTokens(
            accessToken = loginResponse.accessToken,
            refreshToken = loginResponse.refreshToken,
        )
        return loginResponse.onboardingCompleted
    }

    override suspend fun loginWithAppleIdentityToken(identityToken: String): Boolean {
        require(value = identityToken.isNotBlank()) {
            "Apple identity token is empty."
        }
        val loginResponse =
            authRemoteDataSource.loginWithApple(
                request = AppleLoginRequest(identityToken = identityToken),
            )
        saveTokens(
            accessToken = loginResponse.accessToken,
            refreshToken = loginResponse.refreshToken,
        )
        return loginResponse.onboardingCompleted
    }

    override suspend fun getAutoLoginState(): AutoLoginState {
        val accessToken = AuthTokenStore.getAccessToken()
        val refreshToken = AuthTokenStore.getRefreshToken()
        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            return AutoLoginState(
                isLoggedIn = false,
                isOnboardingCompleted = false,
            )
        }

        return try {
            val memberMeResponse = authRemoteDataSource.getMemberMe()
            AutoLoginState(
                isLoggedIn = true,
                isOnboardingCompleted = memberMeResponse.onboardingCompleted,
            )
        } catch (throwable: Throwable) {
            if (throwable is ClientRequestException) {
                val statusCode = throwable.response.status
                val isUnauthorized = statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden
                if (isUnauthorized) {
                    return when (val refreshTokenResult = refreshTokensFromRefreshToken()) {
                        is RefreshTokenResult.Success -> {
                            refreshTokenResult.state
                        }

                        RefreshTokenResult.AuthenticationFailed -> {
                            AuthTokenStore.clearTokens()
                            AutoLoginState(
                                isLoggedIn = false,
                                isOnboardingCompleted = false,
                            )
                        }

                        RefreshTokenResult.TemporaryFailure -> {
                            AutoLoginState(
                                isLoggedIn = false,
                                isOnboardingCompleted = false,
                            )
                        }
                    }
                }
            }
            AuthTokenStore.clearTokens()
            AutoLoginState(
                isLoggedIn = false,
                isOnboardingCompleted = false,
            )
        }
    }

    override suspend fun completeOnboarding(
        preferredDayOff: Int,
        remainingDayOff: Int,
    ) {
        authRemoteDataSource.completeOnboarding(
            request =
                UpdateMemberDayOffRequest(
                    preferredDayOff = preferredDayOff,
                    remainingDayOff = remainingDayOff,
                ),
        )
    }

    override fun clearTokens() {
        AuthTokenStore.clearTokens()
    }

    private fun saveTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        require(value = accessToken.isNotBlank()) {
            "Service access token is empty."
        }
        require(value = refreshToken.isNotBlank()) {
            "Service refresh token is empty."
        }
        AuthTokenStore.updateTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private suspend fun refreshTokensFromRefreshToken(): RefreshTokenResult {
        val storedRefreshToken = AuthTokenStore.getRefreshToken()
        if (storedRefreshToken.isNullOrBlank()) {
            return RefreshTokenResult.AuthenticationFailed
        }

        val refreshResponse =
            try {
                authRemoteDataSource.refreshToken(
                    request = RefreshTokenRequest(refreshToken = storedRefreshToken),
                )
            } catch (clientRequestException: ClientRequestException) {
                val statusCode = clientRequestException.response.status
                val isUnauthorized = statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden
                if (isUnauthorized) {
                    return RefreshTokenResult.AuthenticationFailed
                }
                return RefreshTokenResult.TemporaryFailure
            } catch (throwable: Throwable) {
                return RefreshTokenResult.TemporaryFailure
            }

        saveTokens(
            accessToken = refreshResponse.accessToken,
            refreshToken = refreshResponse.refreshToken,
        )

        val refreshedMemberMeResponse =
            try {
                authRemoteDataSource.getMemberMe()
            } catch (clientRequestException: ClientRequestException) {
                val statusCode = clientRequestException.response.status
                val isUnauthorized = statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden
                if (isUnauthorized) {
                    return RefreshTokenResult.AuthenticationFailed
                }
                return RefreshTokenResult.TemporaryFailure
            } catch (throwable: Throwable) {
                return RefreshTokenResult.TemporaryFailure
            }

        return RefreshTokenResult.Success(
            AutoLoginState(
                isLoggedIn = true,
                isOnboardingCompleted = refreshedMemberMeResponse.onboardingCompleted,
            ),
        )
    }

    private sealed interface RefreshTokenResult {
        data class Success(
            val state: AutoLoginState,
        ) : RefreshTokenResult

        data object AuthenticationFailed : RefreshTokenResult

        data object TemporaryFailure : RefreshTokenResult
    }
}
