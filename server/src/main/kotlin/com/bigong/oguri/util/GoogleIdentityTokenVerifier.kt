package com.bigong.oguri.util

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class GoogleIdentityTokenVerifier(
    @Value("\${google.web-client-id:}")
    private val googleWebClientId: String,
) {
    fun extractGoogleSubject(identityToken: String): String {
        require(value = identityToken.isNotBlank()) {
            "Google identity token is empty."
        }
        if (googleWebClientId.isBlank()) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "GOOGLE_WEB_CLIENT_ID 설정이 필요합니다.")
        }

        val googleIdTokenVerifier =
            GoogleIdTokenVerifier
                .Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(listOf(googleWebClientId))
                .build()

        val verifiedGoogleIdToken =
            runCatching {
                googleIdTokenVerifier.verify(identityToken)
            }.getOrNull()
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 Google identityToken 입니다.")

        val issuer = verifiedGoogleIdToken.payload.issuer
        if (issuer !in ALLOWED_GOOGLE_ISSUERS) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google identityToken issuer가 올바르지 않습니다.")
        }
        return verifiedGoogleIdToken.payload.subject
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google 사용자 식별자(sub)가 없습니다.")
    }

    private companion object {
        private val ALLOWED_GOOGLE_ISSUERS: Set<String> = setOf(
            "accounts.google.com",
            "https://accounts.google.com",
        )
    }
}
