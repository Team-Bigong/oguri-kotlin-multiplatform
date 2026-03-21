package com.bigong.oguri.core.platform

import androidx.activity.ComponentActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.bigong.oguri.core.network.GOOGLE_WEB_CLIENT_ID
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.lang.IllegalStateException
import java.security.SecureRandom
import java.util.Base64

actual suspend fun loginWithGoogle(): Result<String> =
    runCatching {
        val currentActivity = OguriPlatformContextHolder.currentActivity
            ?: error("Current activity is not available for Google login.")
        require(value = GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
            "Google Web Client ID is empty."
        }
        requestGoogleIdentityToken(
            credentialManager = CredentialManager.create(currentActivity),
            activity = currentActivity,
        )
    }

private suspend fun requestGoogleIdentityToken(
    credentialManager: CredentialManager,
    activity: ComponentActivity,
): String {
    val credentialRequests =
        listOf(
            buildGetCredentialRequest(filterByAuthorizedAccounts = true),
            buildGetCredentialRequest(filterByAuthorizedAccounts = false),
            buildSignInWithGoogleCredentialRequest(),
        )
    var latestException: Exception? = null

    credentialRequests.forEachIndexed { requestIndex, getCredentialRequest ->
        try {
            val getCredentialResponse =
                credentialManager.getCredential(
                    context = activity,
                    request = getCredentialRequest,
                )
            return extractGoogleIdentityToken(credential = getCredentialResponse.credential)
        } catch (exception: Exception) {
            latestException = exception
            val shouldRetry =
                requestIndex < LAST_RETRY_INDEX &&
                    (exception is NoCredentialException || exception is GetCredentialCancellationException)
            if (!shouldRetry) {
                throw exception
            }
        }
    }

    throw latestException ?: IllegalStateException("Google credential request failed unexpectedly.")
}

private fun buildGetCredentialRequest(filterByAuthorizedAccounts: Boolean): GetCredentialRequest {
    val getGoogleIdOption =
        GetGoogleIdOption
            .Builder()
            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setAutoSelectEnabled(filterByAuthorizedAccounts)
            .setNonce(generateSecureRandomNonce())
            .build()
    return GetCredentialRequest
        .Builder()
        .addCredentialOption(getGoogleIdOption)
        .build()
}

private fun buildSignInWithGoogleCredentialRequest(): GetCredentialRequest {
    val signInWithGoogleOption =
        GetSignInWithGoogleOption
            .Builder(serverClientId = GOOGLE_WEB_CLIENT_ID)
            .setNonce(generateSecureRandomNonce())
            .build()
    return GetCredentialRequest
        .Builder()
        .addCredentialOption(signInWithGoogleOption)
        .build()
}

private fun extractGoogleIdentityToken(credential: Credential): String {
    if (credential is CustomCredential && isGoogleIdTokenCredentialType(credential.type)) {
        val googleIdTokenCredential =
            try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (exception: GoogleIdTokenParsingException) {
                throw IllegalStateException("Failed to parse Google ID token credential.", exception)
            }
        return googleIdTokenCredential.idToken.takeIf { identityToken -> identityToken.isNotBlank() }
            ?: error("Google ID token is empty.")
    }
    error("Unexpected credential type for Google sign-in.")
}

private fun isGoogleIdTokenCredentialType(credentialType: String): Boolean {
    return credentialType == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
        credentialType == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
}

private fun generateSecureRandomNonce(byteLength: Int = NONCE_BYTE_LENGTH): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}

private const val LAST_RETRY_INDEX: Int = 2
private const val NONCE_BYTE_LENGTH: Int = 32
