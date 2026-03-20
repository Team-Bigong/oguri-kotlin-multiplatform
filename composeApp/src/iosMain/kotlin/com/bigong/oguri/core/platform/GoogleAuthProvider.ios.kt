package com.bigong.oguri.core.platform

actual suspend fun loginWithGoogle(): Result<String> =
    Result.failure(
        exception = UnsupportedOperationException("Google login is not supported on iOS."),
    )
