package com.bigong.oguri.core.network

import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class TravelReauthenticationTest {
    @Test
    fun rejectedRefreshClearsCredentialsButKeepsTheDraftAccountForReauthentication() =
        runTest {
            signIn("first")
            try {
                val original = AuthTokenStore.currentSession()
                AuthTokenStore.refreshTokens(original) { null }

                val expired = AuthTokenStore.currentSession()
                assertNull(expired.tokens)
                assertEquals(original.accountKey, expired.accountKey)
                assertNotEquals(original.version, expired.version)

                signIn("first")
                assertEquals("first", AuthTokenStore.currentSession().accountKey)
                assertNotEquals(expired.version, AuthTokenStore.currentSession().version)
            } finally {
                AuthTokenStore.clearTokens()
            }
        }

    @Test
    fun lateAuthenticationFailureCannotExpireANewerLogin() {
        signIn("first")
        try {
            val previous = AuthTokenStore.currentSession()
            signIn("second")
            val current = AuthTokenStore.currentSession()
            AuthTokenStore.expireSession(previous)
            assertEquals(current, AuthTokenStore.currentSession())
        } finally {
            AuthTokenStore.clearTokens()
        }
    }

    private fun signIn(account: String) {
        val payload = Base64.UrlSafe.encode("""{"sub":"$account"}""".encodeToByteArray())
        AuthTokenStore.updateTokens("header.$payload.signature", "refresh")
    }
}
