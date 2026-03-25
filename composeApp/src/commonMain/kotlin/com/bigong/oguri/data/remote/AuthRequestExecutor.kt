package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.data.remote.model.request.RefreshTokenRequest
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

@Inject
class AuthRequestExecutor(
    private val authRemoteDataSource: AuthRemoteDataSource,
) {
    suspend fun <T> execute(block: suspend () -> T): T {
        return try {
            block()
        } catch (clientRequestException: ClientRequestException) {
            val statusCode = clientRequestException.response.status
            val isAuthError = statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden
            if (!isAuthError) {
                throw clientRequestException
            }

            val staleAccessToken = AuthTokenStore.getAccessToken()
            var isRefreshTokenAuthenticationFailed = false
            val refreshedTokenPair =
                AuthTokenStore.refreshTokens(staleAccessToken = staleAccessToken) {
                    val storedRefreshToken = AuthTokenStore.getRefreshToken() ?: return@refreshTokens null
                    try {
                        val refreshTokenResponse =
                            authRemoteDataSource.refreshToken(
                                request = RefreshTokenRequest(refreshToken = storedRefreshToken),
                            )
                        AuthTokenStore.TokenPair(
                            accessToken = refreshTokenResponse.accessToken,
                            refreshToken = refreshTokenResponse.refreshToken,
                        )
                    } catch (refreshRequestException: ClientRequestException) {
                        val refreshStatusCode = refreshRequestException.response.status
                        val isRefreshAuthError =
                            refreshStatusCode == HttpStatusCode.Unauthorized ||
                                refreshStatusCode == HttpStatusCode.Forbidden
                        if (isRefreshAuthError) {
                            isRefreshTokenAuthenticationFailed = true
                            return@refreshTokens null
                        }
                        throw refreshRequestException
                    }
                }

            if (refreshedTokenPair == null) {
                if (isRefreshTokenAuthenticationFailed) {
                    AuthTokenStore.clearTokens()
                }
                throw clientRequestException
            }

            block()
        }
    }
}
