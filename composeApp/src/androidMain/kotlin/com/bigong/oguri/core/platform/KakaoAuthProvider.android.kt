package com.bigong.oguri.core.platform

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual suspend fun loginWithKakao(): Result<String> {
    return runCatching {
        val applicationContext = OguriPlatformContextHolder.applicationContext
            ?: error("Application context is not initialized for Kakao login.")
        performKakaoLogin(context = applicationContext)
    }
}

private suspend fun performKakaoLogin(context: Context): String {
    return suspendCancellableCoroutine { continuation ->
        val accountCallback: (OAuthToken?, Throwable?) -> Unit = accountCallback@{ token: OAuthToken?, error: Throwable? ->
            if (!continuation.isActive) {
                return@accountCallback
            }

            if (error != null) {
                continuation.resumeWithException(error)
                return@accountCallback
            }

            val accessToken = token?.accessToken
            if (accessToken.isNullOrBlank()) {
                continuation.resumeWithException(IllegalStateException("Kakao login token is empty."))
                return@accountCallback
            }
            continuation.resume(accessToken)
        }

        val isKakaoTalkInstalled = UserApiClient.instance.isKakaoTalkLoginAvailable(context)
        if (!isKakaoTalkInstalled) {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = accountCallback)
            return@suspendCancellableCoroutine
        }

        UserApiClient.instance.loginWithKakaoTalk(context) { token: OAuthToken?, error: Throwable? ->
            if (!continuation.isActive) {
                return@loginWithKakaoTalk
            }

            if (error != null) {
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    continuation.resumeWithException(error)
                    return@loginWithKakaoTalk
                }
                UserApiClient.instance.loginWithKakaoAccount(context, callback = accountCallback)
                return@loginWithKakaoTalk
            }

            val accessToken = token?.accessToken
            if (accessToken.isNullOrBlank()) {
                continuation.resumeWithException(IllegalStateException("Kakao login token is empty."))
                return@loginWithKakaoTalk
            }
            continuation.resume(accessToken)
        }
    }
}
