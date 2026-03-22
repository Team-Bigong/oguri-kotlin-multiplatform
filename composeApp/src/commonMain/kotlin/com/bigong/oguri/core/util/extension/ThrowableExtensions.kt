package com.bigong.oguri.core.util.extension

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode

fun Throwable.isUnauthorized(): Boolean {
    if (this !is ClientRequestException) {
        return false
    }
    val statusCode = response.status
    return statusCode == HttpStatusCode.Unauthorized || statusCode == HttpStatusCode.Forbidden
}
