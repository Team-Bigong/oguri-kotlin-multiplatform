package com.bigong.oguri.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Service
class AdminAuthService(
    @Value("\${admin.username:}")
    private val configuredUsername: String,
    @Value("\${admin.password:}")
    private val configuredPassword: String,
    @Value("\${admin.session-secret:}")
    private val configuredSessionSecret: String,
    @Value("\${admin.session-validity-seconds:28800}")
    private val sessionValiditySeconds: Long,
) {
    fun login(
        username: String,
        password: String,
    ): Pair<String, Long> {
        validateAdminConfiguration()

        if (username != configuredUsername || password != configuredPassword) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "관리자 계정 정보가 올바르지 않습니다.")
        }

        val now = Date()
        val expiration = Date(now.time + sessionValiditySeconds * MILLISECONDS_PER_SECOND)
        val token =
            Jwts
                .builder()
                .subject(username)
                .claim(ADMIN_ROLE_CLAIM_NAME, ADMIN_ROLE_CLAIM_VALUE)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(resolveSigningKey())
                .compact()

        return token to sessionValiditySeconds
    }

    fun validateAccessToken(token: String) {
        validateAdminConfiguration()
        try {
            val parser = Jwts.parser().verifyWith(resolveSigningKey()).build()
            parser.parseSignedClaims(token)
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "관리자 인증 토큰이 유효하지 않습니다.")
        }
    }

    private fun validateAdminConfiguration() {
        if (configuredUsername.isBlank() || configuredPassword.isBlank()) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ADMIN 사용자 계정 설정이 필요합니다.")
        }
        if (configuredSessionSecret.isBlank()) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ADMIN_SESSION_SECRET 설정이 필요합니다.")
        }
        if (configuredSessionSecret.toByteArray(StandardCharsets.UTF_8).size < MINIMUM_SECRET_BYTE_LENGTH) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ADMIN_SESSION_SECRET 길이가 너무 짧습니다.")
        }
    }

    private fun resolveSigningKey(): SecretKey = Keys.hmacShaKeyFor(configuredSessionSecret.toByteArray(StandardCharsets.UTF_8))

    private companion object {
        private const val ADMIN_ROLE_CLAIM_NAME = "role"
        private const val ADMIN_ROLE_CLAIM_VALUE = "ADMIN"
        private const val MILLISECONDS_PER_SECOND = 1000L
        private const val MINIMUM_SECRET_BYTE_LENGTH = 32
    }
}
