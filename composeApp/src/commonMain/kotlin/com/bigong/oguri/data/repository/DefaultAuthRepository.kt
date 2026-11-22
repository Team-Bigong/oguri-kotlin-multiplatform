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
import kotlinx.coroutines.CancellationException

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
        val session = AuthTokenStore.currentSession()
        val tokens = session.tokens
        if (tokens == null || tokens.accessToken.isBlank() || tokens.refreshToken.isBlank()) {
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
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (throwable: Exception) {
            if (throwable is ClientRequestException) {
                val statusCode = throwable.response.status
                val isUnauthorized = statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden
                if (isUnauthorized) {
                    return when (val refreshTokenResult = refreshTokensFromRefreshToken(session)) {
                        is RefreshTokenResult.Success -> {
                            refreshTokenResult.state
                        }

                        RefreshTokenResult.AuthenticationFailed -> {
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

    private suspend fun refreshTokensFromRefreshToken(original: AuthTokenStore.Session): RefreshTokenResult {
        val refreshed =
            try {
                AuthTokenStore.refreshTokens(original) { refreshToken ->
                    try {
                        val response = authRemoteDataSource.refreshToken(RefreshTokenRequest(refreshToken))
                        AuthTokenStore.TokenPair(response.accessToken, response.refreshToken)
                    } catch (exception: ClientRequestException) {
                        val status = exception.response.status
                        if (status != HttpStatusCode.Unauthorized && status != HttpStatusCode.Forbidden) throw exception
                        null
                    }
                } ?: return RefreshTokenResult.AuthenticationFailed
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                return RefreshTokenResult.TemporaryFailure
            }

        val refreshedSession = AuthTokenStore.currentSession()
        if (refreshedSession.version != original.version || refreshedSession.tokens != refreshed) {
            return RefreshTokenResult.TemporaryFailure
        }

        val refreshedMemberMeResponse =
            try {
                authRemoteDataSource.getMemberMe()
            } catch (clientRequestException: ClientRequestException) {
                val statusCode = clientRequestException.response.status
                val isUnauthorized = statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden
                if (isUnauthorized) {
                    AuthTokenStore.expireSession(refreshedSession)
                    return RefreshTokenResult.AuthenticationFailed
                }
                return RefreshTokenResult.TemporaryFailure
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
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
