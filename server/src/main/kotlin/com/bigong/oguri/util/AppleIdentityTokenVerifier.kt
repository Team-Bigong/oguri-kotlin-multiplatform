package com.bigong.oguri.util

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference

@Component
class AppleIdentityTokenVerifier(
    private val objectMapper: ObjectMapper,
    private val authRestTemplate: RestTemplate,
    @param:Value("\${apple.client-id:}")
    private val appleClientId: String
) {
    private val cachedApplePublicKeys = AtomicReference<CachedApplePublicKeys?>()

    fun extractAppleSubject(identityToken: String): String {
        if (appleClientId.isBlank()) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "APPLE_CLIENT_ID 설정이 필요합니다.")
        }

        val identityTokenParts = identityToken.split(".")
        if (identityTokenParts.size != TOKEN_PART_SIZE) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Apple identityToken 형식입니다.")
        }

        val decodedHeader = String(base64UrlDecoder.decode(identityTokenParts[0]), StandardCharsets.UTF_8)
        val tokenHeader = objectMapper.readValue(decodedHeader, AppleIdentityTokenHeader::class.java)
        val applePublicKey = resolveApplePublicKey(tokenHeader.keyIdentifier, tokenHeader.algorithm)

        val claims = try {
            Jwts.parser()
                .verifyWith(applePublicKey)
                .build()
                .parseSignedClaims(identityToken)
                .payload
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 Apple identityToken 입니다.", exception)
        }

        val issuer = claims.issuer
        if (issuer != APPLE_ISSUER) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple identityToken issuer가 올바르지 않습니다.")
        }

        val audienceClaimValue = claims[AUDIENCE_CLAIM_NAME]
        val isAudienceMatched = when (audienceClaimValue) {
            is String -> audienceClaimValue == appleClientId
            is Collection<*> -> audienceClaimValue.any { audienceValue -> audienceValue == appleClientId }
            else -> false
        }
        if (!isAudienceMatched) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple identityToken audience가 올바르지 않습니다.")
        }

        return claims.subject ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple 사용자 식별자(sub)가 없습니다.")
    }

    private fun resolveApplePublicKey(keyIdentifier: String, algorithm: String): PublicKey {
        val applePublicKeyResponse = loadApplePublicKeys()

        val matchedApplePublicKey = applePublicKeyResponse.keys.firstOrNull { applePublicKey ->
            applePublicKey.keyIdentifier == keyIdentifier && applePublicKey.algorithm == algorithm
        } ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "일치하는 Apple 공개키를 찾을 수 없습니다.")

        val modulus = BigInteger(1, base64UrlDecoder.decode(matchedApplePublicKey.modulus))
        val exponent = BigInteger(1, base64UrlDecoder.decode(matchedApplePublicKey.exponent))
        val publicKeySpecification = RSAPublicKeySpec(modulus, exponent)
        return KeyFactory.getInstance(RSA_ALGORITHM_NAME).generatePublic(publicKeySpecification)
    }

    private fun loadApplePublicKeys(): ApplePublicKeyResponse {
        val nowMillis = System.currentTimeMillis()
        val cachedValue = cachedApplePublicKeys.get()
        if (cachedValue != null && nowMillis < cachedValue.expireAtMillis) {
            return cachedValue.response
        }

        val startedAt = System.currentTimeMillis()
        val fetchedResponse = authRestTemplate.getForObject(APPLE_PUBLIC_KEYS_URL, ApplePublicKeyResponse::class.java)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple 공개키를 조회할 수 없습니다.")
        cachedApplePublicKeys.set(
            CachedApplePublicKeys(
                response = fetchedResponse,
                expireAtMillis = nowMillis + APPLE_KEYS_CACHE_MILLIS
            )
        )
        logger.info("Apple public keys refreshed. elapsedMs={}", System.currentTimeMillis() - startedAt)
        return fetchedResponse
    }

    private companion object {
        private const val APPLE_PUBLIC_KEYS_URL: String = "https://appleid.apple.com/auth/keys"
        private const val APPLE_ISSUER: String = "https://appleid.apple.com"
        private const val AUDIENCE_CLAIM_NAME: String = "aud"
        private const val RSA_ALGORITHM_NAME: String = "RSA"
        private const val TOKEN_PART_SIZE: Int = 3
        private const val APPLE_KEYS_CACHE_MILLIS: Long = 60 * 60 * 1000
        private val base64UrlDecoder: Base64.Decoder = Base64.getUrlDecoder()
        private val logger = LoggerFactory.getLogger(AppleIdentityTokenVerifier::class.java)
    }
}

private data class CachedApplePublicKeys(
    val response: ApplePublicKeyResponse,
    val expireAtMillis: Long
)

private data class AppleIdentityTokenHeader(
    @param:JsonProperty("kid")
    val keyIdentifier: String,
    @param:JsonProperty("alg")
    val algorithm: String
)

private data class ApplePublicKeyResponse(
    @param:JsonProperty("keys")
    val keys: List<ApplePublicKey>
)

private data class ApplePublicKey(
    @param:JsonProperty("kid")
    val keyIdentifier: String,
    @param:JsonProperty("alg")
    val algorithm: String,
    @param:JsonProperty("n")
    val modulus: String,
    @param:JsonProperty("e")
    val exponent: String
)
