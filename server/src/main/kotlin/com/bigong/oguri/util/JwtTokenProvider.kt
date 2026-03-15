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
    private val secretKeyString: String
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKeyString.toByteArray())
    
    // Access Token 만료 시간: 1시간
    private val accessTokenValidityInMilliseconds: Long = 3600000 
    // Refresh Token 만료 시간: 14일
    private val refreshTokenValidityInMilliseconds: Long = 1209600000

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
}
