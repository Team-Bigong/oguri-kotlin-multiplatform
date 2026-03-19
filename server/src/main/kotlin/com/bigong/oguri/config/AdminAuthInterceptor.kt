package com.bigong.oguri.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import com.bigong.oguri.service.AdminAuthService
import org.springframework.web.server.ResponseStatusException

@Component
class AdminAuthInterceptor(
    private val adminAuthService: AdminAuthService
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.method == HttpMethod.OPTIONS.name()) {
            return true
        }
        if (request.requestURI == ADMIN_LOGIN_PATH && request.method == HttpMethod.POST.name()) {
            return true
        }

        val authorizationHeader = request.getHeader(AUTHORIZATION_HEADER_NAME)
        if (authorizationHeader.isNullOrBlank() || !authorizationHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "관리자 인증 토큰이 필요합니다.")
        }

        val token = authorizationHeader.removePrefix(BEARER_TOKEN_PREFIX).trim()
        if (token.isBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "관리자 인증 토큰이 비어 있습니다.")
        }

        adminAuthService.validateAccessToken(token)
        return true
    }

    private companion object {
        private const val AUTHORIZATION_HEADER_NAME = "Authorization"
        private const val BEARER_TOKEN_PREFIX = "Bearer "
        private const val ADMIN_LOGIN_PATH = "/api/admin/v1/auth/login"
    }
}
