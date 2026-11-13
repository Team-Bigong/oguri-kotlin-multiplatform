package com.bigong.oguri.core.network

import com.bigong.oguri.data.local.TokenLocalDataSource
import com.bigong.oguri.data.local.travelRecoveryStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64

object AuthTokenStore {
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String,
    )

    private val refreshTokenMutex = Mutex()
    private var tokenLocalDataSource: TokenLocalDataSource? = null

    internal data class Session(
        val version: Long,
        val tokens: TokenPair?,
        val accountKey: String? = tokens?.accessToken?.let(::accountSubject),
    )

    private val mutableSession: MutableStateFlow<Session> = MutableStateFlow(Session(0, null))
    internal val sessions: StateFlow<Session> = mutableSession
    private val session: Session get() = mutableSession.value

    internal fun currentSession(): Session = session

    fun initialize(localDataSource: TokenLocalDataSource) {
        if (tokenLocalDataSource != null) {
            return
        }
        localDataSource.initialize()
        tokenLocalDataSource = localDataSource
    }

    fun bootstrapFromLocalDataSource() {
        if (session.tokens != null) return
        val localDataSource = tokenLocalDataSource ?: return
        val accessToken = localDataSource.readAccessToken() ?: return
        val refreshToken = localDataSource.readRefreshToken() ?: return
        mutableSession.value = Session(session.version + 1, TokenPair(accessToken, refreshToken))
    }

    fun getAccessToken(): String? = session.tokens?.accessToken

    fun getRefreshToken(): String? = session.tokens?.refreshToken

    fun updateTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        val previous = session.accountKey
        val updated = Session(session.version + 1, TokenPair(accessToken, refreshToken))
        saveSession(updated)
        if (previous != null && previous != updated.accountKey) purgeTravelRecovery()
    }

    fun clearTokens() {
        saveSession(Session(session.version + 1, null))
        purgeTravelRecovery()
    }

    private fun saveSession(updated: Session) {
        mutableSession.value = updated
        val tokens = updated.tokens
        if (tokens == null) {
            tokenLocalDataSource?.clearTokens()
        } else {
            tokenLocalDataSource?.writeTokens(tokens.accessToken, tokens.refreshToken)
        }
    }

    internal suspend fun refreshTokens(
        original: Session,
        refresh: suspend (String) -> TokenPair?,
    ): TokenPair? =
        refreshTokenMutex.withLock {
            val current = session
            if (current.version != original.version) return@withLock null
            val tokens = current.tokens ?: return@withLock null
            if (tokens != original.tokens) return@withLock tokens
            val refreshed = refresh(tokens.refreshToken)
            if (session.version != original.version) return@withLock null
            val updated =
                if (refreshed == null) {
                    Session(current.version + 1, null, current.accountKey)
                } else {
                    Session(current.version, refreshed)
                }
            saveSession(updated)
            refreshed
        }
}

private fun accountSubject(accessToken: String): String? =
    runCatching {
        val payload = accessToken.split('.')[1]
        val text =
            Base64.UrlSafe
                .withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                .decode(payload)
                .decodeToString()
        Json
            .parseToJsonElement(text)
            .jsonObject["sub"]
            ?.jsonPrimitive
            ?.content
            ?.takeIf { it.isNotBlank() }
    }.getOrNull()

private fun purgeTravelRecovery() {
    runCatching { travelRecoveryStorage().access { _, write -> write(null) } }
}
