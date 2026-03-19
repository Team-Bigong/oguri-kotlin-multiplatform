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
            val refreshedTokenPair =
                AuthTokenStore.refreshTokens(staleAccessToken = staleAccessToken) {
                    val storedRefreshToken = AuthTokenStore.getRefreshToken() ?: return@refreshTokens null
                    runCatching {
                        authRemoteDataSource.refreshToken(
                            request = RefreshTokenRequest(refreshToken = storedRefreshToken),
                        )
                    }.getOrNull()?.let { refreshTokenResponse ->
                        AuthTokenStore.TokenPair(
                            accessToken = refreshTokenResponse.accessToken,
                            refreshToken = refreshTokenResponse.refreshToken,
                        )
                    }
                }

            if (refreshedTokenPair == null) {
                AuthTokenStore.clearTokens()
                throw clientRequestException
            }

            block()
        }
    }
}
