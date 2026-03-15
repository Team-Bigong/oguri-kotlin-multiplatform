package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.AuthTokenStore
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header

private const val USER_ID_HEADER_NAME: String = "X-USER-ID"
private const val DEFAULT_USER_ID: String = "GUEST"

internal fun HttpRequestBuilder.appendUserIdHeaderWhenGuest() {
    val accessToken: String? = AuthTokenStore.getAccessToken()
    if (accessToken.isNullOrBlank()) {
        header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
    }
}
