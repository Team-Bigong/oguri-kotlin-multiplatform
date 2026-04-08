package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.BASE_URL
import com.bigong.oguri.data.remote.model.request.AppleLoginRequest
import com.bigong.oguri.data.remote.model.request.GoogleLoginRequest
import com.bigong.oguri.data.remote.model.request.KakaoLoginRequest
import com.bigong.oguri.data.remote.model.request.RefreshTokenRequest
import com.bigong.oguri.data.remote.model.request.UpdateMemberDayOffRequest
import com.bigong.oguri.data.remote.model.response.AuthLoginResponse
import com.bigong.oguri.data.remote.model.response.MemberMeResponse
import com.bigong.oguri.data.remote.model.response.RefreshTokenResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

@Inject
class KtorAuthRemoteDataSource(
    private val httpClient: HttpClient,
) : AuthRemoteDataSource {
    override suspend fun loginWithKakao(request: KakaoLoginRequest): AuthLoginResponse {
        val requestUrl = "$BASE_URL$AUTH_LOGIN_KAKAO_API_PATH"
        return httpClient
            .post(requestUrl) {
                headers.remove(HttpHeaders.Authorization)
                headers.remove(USER_ID_HEADER_NAME)
                headers[HttpHeaders.ContentType] = ContentType.Application.Json.toString()
                setBody(request)
            }.body()
    }

    override suspend fun loginWithGoogle(request: GoogleLoginRequest): AuthLoginResponse {
        val requestUrl = "$BASE_URL$AUTH_LOGIN_GOOGLE_API_PATH"
        return httpClient
            .post(requestUrl) {
                headers.remove(HttpHeaders.Authorization)
                headers.remove(USER_ID_HEADER_NAME)
                headers[HttpHeaders.ContentType] = ContentType.Application.Json.toString()
                setBody(request)
            }.body()
    }

    override suspend fun loginWithGoogle(request: GoogleLoginRequest): AuthLoginResponse {
        val requestUrl = "$DEBUG_BASE_URL$AUTH_LOGIN_GOOGLE_API_PATH"
        return httpClient
            .post(requestUrl) {
                headers.remove(HttpHeaders.Authorization)
                headers.remove(USER_ID_HEADER_NAME)
                headers[HttpHeaders.ContentType] = ContentType.Application.Json.toString()
                setBody(request)
            }.body()
    }

    override suspend fun loginWithApple(request: AppleLoginRequest): AuthLoginResponse {
        val requestUrl = "$BASE_URL$AUTH_LOGIN_APPLE_API_PATH"
        return httpClient
            .post(requestUrl) {
                headers.remove(HttpHeaders.Authorization)
                headers.remove(USER_ID_HEADER_NAME)
                headers[HttpHeaders.ContentType] = ContentType.Application.Json.toString()
                setBody(request)
            }.body()
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse {
        val requestUrl = "$BASE_URL$AUTH_REFRESH_API_PATH"
        return httpClient
            .post(requestUrl) {
                headers.remove(HttpHeaders.Authorization)
                headers.remove(USER_ID_HEADER_NAME)
                headers[HttpHeaders.ContentType] = ContentType.Application.Json.toString()
                setBody(request)
            }.body()
    }

    override suspend fun getMemberMe(): MemberMeResponse {
        val requestUrl = "$BASE_URL$MEMBER_ME_API_PATH"
        return httpClient
            .get(requestUrl)
            .body()
    }

    override suspend fun completeOnboarding(request: UpdateMemberDayOffRequest) {
        val requestUrl = "$BASE_URL$MEMBER_ONBOARDING_API_PATH"
        httpClient.post(requestUrl) {
            headers[HttpHeaders.ContentType] = ContentType.Application.Json.toString()
            setBody(request)
        }
    }

    private companion object {
        private const val AUTH_LOGIN_KAKAO_API_PATH = "/api/v1/auth/login/kakao"
        private const val AUTH_LOGIN_GOOGLE_API_PATH = "/api/v1/auth/login/google"
        private const val AUTH_LOGIN_APPLE_API_PATH = "/api/v1/auth/login/apple"
        private const val AUTH_REFRESH_API_PATH = "/api/v1/auth/refresh"
        private const val MEMBER_ME_API_PATH = "/api/v1/members/me"
        private const val MEMBER_ONBOARDING_API_PATH = "/api/v1/members/onboarding"
        private const val USER_ID_HEADER_NAME = "X-USER-ID"
    }
}
