package com.bigong.oguri.data.remote

import com.bigong.oguri.core.network.AuthTokenStore
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header

private const val USER_ID_HEADER_NAME = "X-USER-ID"
private const val DEFAULT_USER_ID = "GUEST"

internal fun HttpRequestBuilder.appendUserIdHeaderWhenGuest() {
    val accessToken = AuthTokenStore.getAccessToken()
    val hasExplicitUserIdHeader = headers.contains(name = USER_ID_HEADER_NAME)
    if (accessToken.isNullOrBlank() && !hasExplicitUserIdHeader) {
        header(USER_ID_HEADER_NAME, DEFAULT_USER_ID)
    }
}
