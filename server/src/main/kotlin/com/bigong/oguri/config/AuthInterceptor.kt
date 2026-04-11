package com.bigong.oguri.config

import com.bigong.oguri.util.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == HttpMethod.OPTIONS.name()) {
            request.setAttribute(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE, AuthContext.GUEST_MEMBER_ID)
            return true
        }

        val memberId = resolveMemberIdFromAuthorization(request)
        request.setAttribute(AuthContext.AUTHENTICATED_MEMBER_ID_ATTRIBUTE, memberId)

        if (isProtectedEndpoint(request) && memberId == AuthContext.GUEST_MEMBER_ID) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요한 기능입니다.")
        }
        return true
    }

    private fun resolveMemberIdFromAuthorization(request: HttpServletRequest): String {
        val authorizationHeader = request.getHeader(AUTHORIZATION_HEADER_NAME) ?: return AuthContext.GUEST_MEMBER_ID
        if (!authorizationHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization 헤더 형식이 올바르지 않습니다.")
        }

        val token = authorizationHeader.removePrefix(BEARER_TOKEN_PREFIX).trim()
        if (token.isBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "액세스 토큰이 비어 있습니다.")
        }
        if (!jwtTokenProvider.validateToken(token)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 액세스 토큰입니다.")
        }
        return jwtTokenProvider.getMemberId(token)
    }

    private fun isProtectedEndpoint(request: HttpServletRequest): Boolean {
        val requestPath = request.requestURI
        val requestMethod = request.method.uppercase()

        return when {
            requestPath == "/api/v1/members/me" && requestMethod == HttpMethod.GET.name() -> true
            requestPath == "/api/v1/members/me" && requestMethod == HttpMethod.DELETE.name() -> true
            requestPath == "/api/v1/members/day-off" && requestMethod == HttpMethod.POST.name() -> true
            requestPath == "/api/v1/members/onboarding" && requestMethod == HttpMethod.POST.name() -> true
            requestPath == "/api/v1/members/saved-recommendations" &&
                (requestMethod == HttpMethod.POST.name() || requestMethod == HttpMethod.DELETE.name()) -> true
            requestPath.startsWith("/api/v1/members/saved-destinations/") &&
                (requestMethod == HttpMethod.POST.name() || requestMethod == HttpMethod.DELETE.name()) -> true
            else -> false
        }
    }

    private companion object {
        private const val AUTHORIZATION_HEADER_NAME: String = "Authorization"
        private const val BEARER_TOKEN_PREFIX: String = "Bearer "
    }
}
