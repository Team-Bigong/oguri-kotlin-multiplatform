package com.bigong.oguri.core.platform

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val KAKAO_OAUTH_AUTHORIZE_ENDPOINT = "https://kauth.kakao.com/oauth/authorize"
private const val KAKAO_TALK_OAUTH_AUTHORIZE_ENDPOINT = "kakaokompassauth://authorize"
private const val KAKAO_OAUTH_TOKEN_ENDPOINT = "https://kauth.kakao.com/oauth/token"
private const val OAUTH_RESPONSE_TYPE_CODE = "code"
private const val OAUTH_GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code"
private const val KAKAO_TALK_SCHEME_PREFIX = "kakaokompassauth://"

private var webAuthenticationSession: ASWebAuthenticationSession? = null
private var kakaoTalkLoginContinuation: CancellableContinuation<String>? = null
private val webAuthenticationPresentationContextProvider: ASWebAuthenticationPresentationContextProvidingProtocol =
    WebAuthenticationPresentationContextProvider()

actual suspend fun loginWithKakao(): Result<String> {
    val kakaoNativeAppKey = iosKakaoNativeAppKey()
    if (kakaoNativeAppKey.isBlank()) {
        return Result.failure(IllegalStateException("Kakao native app key is empty."))
    }
    return runCatching {
        val authorizationCode = requestAuthorizationCode(kakaoNativeAppKey = kakaoNativeAppKey)
        requestKakaoAccessToken(
            authorizationCode = authorizationCode,
            kakaoNativeAppKey = kakaoNativeAppKey,
        )
            ?: throw IllegalStateException("Failed to issue Kakao access token.")
    }
}

private suspend fun requestAuthorizationCode(kakaoNativeAppKey: String): String {
    if (isKakaoTalkLoginAvailable()) {
        runCatching {
            return requestAuthorizationCodeByKakaoTalk(kakaoNativeAppKey = kakaoNativeAppKey)
        }
    }
    return requestAuthorizationCodeByWeb(kakaoNativeAppKey = kakaoNativeAppKey)
}

private suspend fun requestAuthorizationCodeByKakaoTalk(kakaoNativeAppKey: String): String {
    val callbackScheme = "kakao$kakaoNativeAppKey"
    val redirectUri = "$callbackScheme://oauth"
    val state = NSUUID().UUIDString
    val authorizeUrl =
        "$KAKAO_TALK_OAUTH_AUTHORIZE_ENDPOINT" +
            "?response_type=$OAUTH_RESPONSE_TYPE_CODE" +
            "&client_id=${kakaoNativeAppKey.encodeURLParameter()}" +
            "&redirect_uri=${redirectUri.encodeURLParameter()}" +
            "&state=${state.encodeURLParameter()}"

    val authorizeNsUrl = NSURL.URLWithString(authorizeUrl) ?: throw IllegalStateException("Invalid KakaoTalk authorize URL.")

    return suspendCancellableCoroutine { continuation ->
        kakaoTalkLoginContinuation = continuation
        continuation.invokeOnCancellation {
            if (kakaoTalkLoginContinuation === continuation) {
                kakaoTalkLoginContinuation = null
            }
        }
        UIApplication.sharedApplication.openURL(
            url = authorizeNsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = { opened ->
                if (!opened && continuation.isActive) {
                    if (kakaoTalkLoginContinuation === continuation) {
                        kakaoTalkLoginContinuation = null
                    }
                    continuation.resumeWithException(IllegalStateException("Failed to open KakaoTalk app."))
                }
            },
        )
    }
}

private suspend fun requestAuthorizationCodeByWeb(kakaoNativeAppKey: String): String {
    val callbackScheme = "kakao$kakaoNativeAppKey"
    val redirectUri = "$callbackScheme://oauth"
    val authorizeUrl =
        "$KAKAO_OAUTH_AUTHORIZE_ENDPOINT" +
            "?response_type=$OAUTH_RESPONSE_TYPE_CODE" +
            "&client_id=${kakaoNativeAppKey.encodeURLParameter()}" +
            "&redirect_uri=${redirectUri.encodeURLParameter()}"

    val authorizeNsUrl = NSURL.URLWithString(authorizeUrl) ?: throw IllegalStateException("Invalid authorize URL.")

    return suspendCancellableCoroutine { continuation ->
        val session =
            ASWebAuthenticationSession(
                uRL = authorizeNsUrl,
                callbackURLScheme = callbackScheme,
            ) { callbackUrl, error ->
                webAuthenticationSession = null
                if (!continuation.isActive) {
                    return@ASWebAuthenticationSession
                }

                if (error != null) {
                    continuation.resumeWithException(
                        IllegalStateException(error.localizedDescription),
                    )
                    return@ASWebAuthenticationSession
                }

                val callbackUrlString = callbackUrl?.absoluteString ?: ""
                val authorizationCode = callbackUrlString.extractQueryValue(name = "code")
                if (authorizationCode.isNullOrBlank()) {
                    continuation.resumeWithException(IllegalStateException("Authorization code is missing."))
                    return@ASWebAuthenticationSession
                }
                continuation.resume(authorizationCode)
            }
        webAuthenticationSession = session
        session.presentationContextProvider = webAuthenticationPresentationContextProvider
        session.prefersEphemeralWebBrowserSession = false
        session.start()
    }
}

fun handleKakaoLoginOpenUrl(url: String) {
    val continuation = kakaoTalkLoginContinuation ?: return
    if (!continuation.isActive) {
        kakaoTalkLoginContinuation = null
        return
    }

    val callbackUrlString = url.trim()
    val authorizationCode = callbackUrlString.extractQueryValue(name = "code")
    if (!authorizationCode.isNullOrBlank()) {
        kakaoTalkLoginContinuation = null
        continuation.resume(authorizationCode)
        return
    }

    val errorDescription =
        callbackUrlString.extractQueryValue(name = "error_description")
            ?: callbackUrlString.extractQueryValue(name = "error")
            ?: "Kakao login failed."
    kakaoTalkLoginContinuation = null
    continuation.resumeWithException(IllegalStateException(errorDescription))
}

private suspend fun requestKakaoAccessToken(
    authorizationCode: String,
    kakaoNativeAppKey: String,
): String? {
    val callbackScheme = "kakao$kakaoNativeAppKey"
    val redirectUri = "$callbackScheme://oauth"

    val httpClient =
        HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

    return try {
        val response =
            httpClient
                .submitForm(
                    url = KAKAO_OAUTH_TOKEN_ENDPOINT,
                    formParameters =
                        Parameters.build {
                            append("grant_type", OAUTH_GRANT_TYPE_AUTHORIZATION_CODE)
                            append("client_id", kakaoNativeAppKey)
                            append("redirect_uri", redirectUri)
                            append("code", authorizationCode)
                        },
                ).body<KakaoOAuthTokenResponse>()
        response.accessToken
    } finally {
        httpClient.close()
    }
}

private fun String.extractQueryValue(name: String): String? =
    substringAfter("?", missingDelimiterValue = "")
        .split("&")
        .firstOrNull { token -> token.startsWith("$name=") }
        ?.substringAfter("=")
        ?.takeIf { value -> value.isNotBlank() }

private fun iosKakaoNativeAppKey(): String = (NSBundle.mainBundle.objectForInfoDictionaryKey("KEY_KAKAO") as? String)?.trim().orEmpty()

private fun isKakaoTalkLoginAvailable(): Boolean {
    val kakaoTalkAuthUrl = NSURL.URLWithString(KAKAO_TALK_SCHEME_PREFIX) ?: return false
    return UIApplication.sharedApplication.canOpenURL(kakaoTalkAuthUrl)
}

private class WebAuthenticationPresentationContextProvider :
    NSObject(),
    ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor =
        UIApplication.sharedApplication.keyWindow ?: UIWindow()
}

@Serializable
private data class KakaoOAuthTokenResponse(
    @SerialName("access_token")
    val accessToken: String? = null,
)
