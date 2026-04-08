package com.bigong.oguri.core.network

val BASE_URL: String
    get() =
        if (isDebugPlatformBuild()) {
            DEBUG_BASE_URL
        } else {
            RELEASE_BASE_URL
        }

internal expect fun isDebugPlatformBuild(): Boolean
