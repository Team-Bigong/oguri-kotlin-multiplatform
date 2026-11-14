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
    suspend fun <Result> execute(block: suspend () -> Result): Result {
        val session = AuthTokenStore.currentSession()
        return try {
            block()
        } catch (exception: ClientRequestException) {
            val status = exception.response.status
            if (status != HttpStatusCode.Unauthorized && status != HttpStatusCode.Forbidden) throw exception
            if (refresh(session) == null || AuthTokenStore.currentSession().version != session.version) throw exception
            block()
        }
    }

    internal suspend fun <Result> executeForSession(
        sessionVersion: Long,
        block: suspend (String) -> Result,
    ): Result {
        val session = AuthTokenStore.currentSession()
        if (session.version != sessionVersion) throw AuthSessionChangedException()
        val accessToken = session.tokens?.accessToken ?: throw AuthSessionChangedException()
        val result =
            try {
                block(accessToken)
            } catch (exception: ClientRequestException) {
                if (exception.response.status != HttpStatusCode.Unauthorized) throw exception
                val refreshed = refresh(session) ?: throw exception
                if (AuthTokenStore.currentSession().version != session.version) throw AuthSessionChangedException()
                block(refreshed.accessToken)
            }
        if (AuthTokenStore.currentSession().version != session.version) throw AuthSessionChangedException()
        return result
    }

    private suspend fun refresh(session: AuthTokenStore.Session): AuthTokenStore.TokenPair? =
        AuthTokenStore.refreshTokens(session) { refreshToken ->
            try {
                val response = authRemoteDataSource.refreshToken(RefreshTokenRequest(refreshToken = refreshToken))
                AuthTokenStore.TokenPair(response.accessToken, response.refreshToken)
            } catch (exception: ClientRequestException) {
                val status = exception.response.status
                if (status != HttpStatusCode.Unauthorized && status != HttpStatusCode.Forbidden) throw exception
                null
            }
        }
}

internal class AuthSessionChangedException : Exception()
