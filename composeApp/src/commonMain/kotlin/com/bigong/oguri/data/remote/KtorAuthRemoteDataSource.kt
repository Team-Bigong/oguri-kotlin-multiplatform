package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.DEBUG_BASE_URL
import com.bigong.oguri.data.remote.model.request.KakaoLoginRequest
import com.bigong.oguri.data.remote.model.request.RefreshTokenRequest
import com.bigong.oguri.data.remote.model.response.KakaoLoginResponse
import com.bigong.oguri.data.remote.model.response.RefreshTokenResponse
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

@Inject
class KtorAuthRemoteDataSource(
    private val httpClient: HttpClient,
) : AuthRemoteDataSource {
    override suspend fun loginWithKakao(request: KakaoLoginRequest): KakaoLoginResponse {
        val requestUrl = "$DEBUG_BASE_URL$AUTH_LOGIN_KAKAO_API_PATH"
        return httpClient
            .post(requestUrl) {
                headers.remove(HttpHeaders.Authorization)
                headers[HttpHeaders.ContentType] = ContentType.Application.Json.toString()
                setBody(request)
            }.body()
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse {
        val requestUrl = "$DEBUG_BASE_URL$AUTH_REFRESH_API_PATH"
        return httpClient
            .post(requestUrl) {
                headers.remove(HttpHeaders.Authorization)
                headers[HttpHeaders.ContentType] = ContentType.Application.Json.toString()
                setBody(request)
            }.body()
    }

    private companion object {
        private const val AUTH_LOGIN_KAKAO_API_PATH = "/api/v1/auth/login/kakao"
        private const val AUTH_REFRESH_API_PATH = "/api/v1/auth/refresh"
    }
}
