package com.bigong.oguri.core.platform

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

interface KakaoLoginHost {
    fun login(request: KakaoLoginRequest)
}

class KakaoLoginRequest internal constructor(
    private val continuation: CancellableContinuation<Result<String>>,
) {
    fun complete(accessToken: String?, errorMessage: String?) {
        val result =
            if (!accessToken.isNullOrBlank()) {
                Result.success(accessToken)
            } else {
                Result.failure(IllegalStateException(errorMessage ?: "Kakao login failed."))
            }
        continuation.resume(result)
    }
}

private var kakaoLoginHost: KakaoLoginHost? = null

fun installKakaoLoginHost(host: KakaoLoginHost) {
    kakaoLoginHost = host
}

actual suspend fun loginWithKakao(): Result<String> =
    withContext(Dispatchers.Main.immediate) {
        val host = kakaoLoginHost
            ?: return@withContext Result.failure(IllegalStateException("Kakao login is not configured."))
        suspendCancellableCoroutine { continuation ->
            host.login(KakaoLoginRequest(continuation))
        }
    }
