package com.bigong.oguri.core.platform

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private var appleAuthorizationDelegate: AppleAuthorizationDelegate? = null

actual suspend fun loginWithApple(): Result<String> {
    return runCatching {
        performAppleAuthorization()
    }
}

private suspend fun performAppleAuthorization(): String {
    return suspendCancellableCoroutine { continuation ->
        val appleIdProvider = ASAuthorizationAppleIDProvider()
        val appleAuthorizationRequest = appleIdProvider.createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
        }
        val authorizationController =
            ASAuthorizationController(
                authorizationRequests = listOf(appleAuthorizationRequest),
            )
        val delegate =
            AppleAuthorizationDelegate(
                onAuthorized = { tokenOrUserId ->
                    if (continuation.isActive) {
                        continuation.resume(tokenOrUserId)
                    }
                    appleAuthorizationDelegate = null
                },
                onFailed = { throwable ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(throwable)
                    }
                    appleAuthorizationDelegate = null
                },
            )
        appleAuthorizationDelegate = delegate
        authorizationController.delegate = delegate
        authorizationController.presentationContextProvider = delegate
        continuation.invokeOnCancellation {
            appleAuthorizationDelegate = null
        }
        authorizationController.performRequests()
    }
}

private class AppleAuthorizationDelegate(
    private val onAuthorized: (String) -> Unit,
    private val onFailed: (Throwable) -> Unit,
) : NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization,
    ) {
        val appleCredential =
            didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
                ?: run {
                    onFailed(IllegalStateException("Apple credential is missing."))
                    return
                }
        val token =
            appleCredential.user
        if (token.isBlank()) {
            onFailed(IllegalStateException("Apple identity token is empty."))
            return
        }
        onAuthorized(token)
    }

    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError: platform.Foundation.NSError,
    ) {
        val message = didCompleteWithError.localizedDescription
        onFailed(IllegalStateException(message))
    }

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor {
        return UIApplication.sharedApplication.keyWindow ?: UIWindow()
    }
}
