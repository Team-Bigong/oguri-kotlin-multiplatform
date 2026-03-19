package com.bigong.oguri.core.network

import com.bigong.oguri.data.local.TokenLocalDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile

object AuthTokenStore {
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String,
    )

    private val refreshTokenMutex = Mutex()
    private var tokenLocalDataSource: TokenLocalDataSource? = null

    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var refreshToken: String? = null

    fun initialize(localDataSource: TokenLocalDataSource) {
        if (tokenLocalDataSource != null) {
            return
        }
        localDataSource.initialize()
        tokenLocalDataSource = localDataSource
    }

    fun bootstrapFromLocalDataSource() {
        if (!accessToken.isNullOrBlank() || !refreshToken.isNullOrBlank()) {
            return
        }
        val localDataSource = tokenLocalDataSource ?: return
        accessToken = localDataSource.readAccessToken()
        refreshToken = localDataSource.readRefreshToken()
    }

    fun getAccessToken(): String? = accessToken

    fun getRefreshToken(): String? = refreshToken

    fun updateTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        tokenLocalDataSource?.writeTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    fun clearTokens() {
        accessToken = null
        refreshToken = null
        tokenLocalDataSource?.clearTokens()
    }

    suspend fun refreshTokens(
        staleAccessToken: String?,
        refresh: suspend () -> TokenPair?,
    ): TokenPair? {
        return refreshTokenMutex.withLock {
            val latestAccessToken = accessToken
            if (latestAccessToken != null && latestAccessToken != staleAccessToken) {
                val latestRefreshToken = refreshToken ?: return@withLock null
                return@withLock TokenPair(
                    accessToken = latestAccessToken,
                    refreshToken = latestRefreshToken,
                )
            }

            val refreshedTokenPair = refresh()
            if (refreshedTokenPair != null) {
                updateTokens(
                    accessToken = refreshedTokenPair.accessToken,
                    refreshToken = refreshedTokenPair.refreshToken,
                )
            }
            return@withLock refreshedTokenPair
        }
    }
}
