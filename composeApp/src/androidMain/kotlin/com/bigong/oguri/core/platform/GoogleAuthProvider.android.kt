package com.bigong.oguri.core.platform

import com.bigong.oguri.core.network.GOOGLE_WEB_CLIENT_ID
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom
import java.util.Base64

actual suspend fun loginWithGoogle(): Result<String> =
    runCatching {
        val currentActivity = OguriPlatformContextHolder.currentActivity
            ?: error("Current activity is not available for Google login.")
        require(value = GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
            "Google Web Client ID is empty."
        }
        val signInWithGoogleOption =
            GetSignInWithGoogleOption
                .Builder(serverClientId = GOOGLE_WEB_CLIENT_ID)
                .setNonce(generateSecureRandomNonce())
                .build()
        val getCredentialRequest =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
        val credentialManager = CredentialManager.create(currentActivity)
        val getCredentialResponse =
            credentialManager.getCredential(
                context = currentActivity,
                request = getCredentialRequest,
            )
        val credential = getCredentialResponse.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential =
                try {
                    GoogleIdTokenCredential.createFrom(credential.data)
                } catch (exception: GoogleIdTokenParsingException) {
                    throw IllegalStateException("Failed to parse Google ID token credential.", exception)
                }
            googleIdTokenCredential.idToken.takeIf { identityToken -> identityToken.isNotBlank() }
                ?: error("Google ID token is empty.")
        } else {
            error("Unexpected credential type for Google sign-in.")
        }
    }

private fun generateSecureRandomNonce(byteLength: Int = NONCE_BYTE_LENGTH): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}

private const val NONCE_BYTE_LENGTH: Int = 32
