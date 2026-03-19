package com.bigong.oguri.core.platform

actual suspend fun loginWithApple(): Result<String> =
    Result.failure(
        exception = UnsupportedOperationException("Apple login is not supported on Android."),
    )
