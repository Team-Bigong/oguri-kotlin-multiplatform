package com.bigong.oguri.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${JWT_SECRET:very-long-and-secure-secret-key-at-least-32-chars-long}")
    private val secretKeyString: String,
    @Value("\${jwt.access-token-validity-seconds:1800}")
    private val accessTokenValidityInSeconds: Long,
    @Value("\${jwt.refresh-token-validity-seconds:1209600}")
    private val refreshTokenValidityInSeconds: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKeyString.toByteArray())

    private val accessTokenValidityInMilliseconds: Long =
        accessTokenValidityInSeconds.coerceAtLeast(MINIMUM_TOKEN_VALIDITY_IN_SECONDS) * MILLISECONDS_PER_SECOND

    private val refreshTokenValidityInMilliseconds: Long =
        refreshTokenValidityInSeconds.coerceAtLeast(MINIMUM_TOKEN_VALIDITY_IN_SECONDS) * MILLISECONDS_PER_SECOND

    fun createAccessToken(memberId: String): String {
        return createToken(memberId, accessTokenValidityInMilliseconds)
    }

    fun createRefreshToken(memberId: String): String {
        return createToken(memberId, refreshTokenValidityInMilliseconds)
    }

    private fun createToken(subject: String, validityInMilliseconds: Long): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun getMemberId(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            !claims.payload.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    private companion object {
        private const val MILLISECONDS_PER_SECOND: Long = 1000
        private const val MINIMUM_TOKEN_VALIDITY_IN_SECONDS: Long = 60
    }
}
