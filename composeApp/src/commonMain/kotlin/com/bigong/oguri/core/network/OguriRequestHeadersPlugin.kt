package com.bigong.oguri.core.network

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import io.ktor.util.AttributeKey

private const val USER_ID_HEADER_NAME = "X-USER-ID"
private const val DEFAULT_USER_ID = "GUEST"
private const val AUTH_API_PREFIX = "/api/v1/auth/"

internal val OmitAuthentication: AttributeKey<Boolean> = AttributeKey("OmitAuthentication")

internal val RequestAccessToken: AttributeKey<String> = AttributeKey("RequestAccessToken")

val OguriRequestHeadersPlugin =
    createClientPlugin(name = "OguriRequestHeadersPlugin") {
        onRequest { request, _ ->
            val path = request.url.toString()
            if (path.startsWith(prefix = AUTH_API_PREFIX) || request.attributes.getOrNull(OmitAuthentication) == true) {
                request.headers.remove(name = HttpHeaders.Authorization)
                request.headers.remove(name = USER_ID_HEADER_NAME)
                return@onRequest
            }

            val accessToken = request.attributes.getOrNull(RequestAccessToken) ?: AuthTokenStore.getAccessToken()
            if (accessToken.isNullOrBlank()) {
                request.headers.remove(name = HttpHeaders.Authorization)
                request.headers.remove(name = USER_ID_HEADER_NAME)
                request.headers.append(name = USER_ID_HEADER_NAME, value = DEFAULT_USER_ID)
                return@onRequest
            }

            request.headers.remove(name = USER_ID_HEADER_NAME)
            request.headers.remove(name = HttpHeaders.Authorization)
            request.headers.append(name = HttpHeaders.Authorization, value = "Bearer $accessToken")
        }
    }
