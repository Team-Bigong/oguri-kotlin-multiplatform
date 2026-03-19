package com.bigong.oguri.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    goBackTrigger: Int,
    onCanGoBackChange: (Boolean) -> Unit,
    onCurrentUrlChange: (String?) -> Unit,
    modifier: Modifier,
) {
    val navigationDelegate =
        remember(onCanGoBackChange, onCurrentUrlChange) {
            object : NSObject(), WKNavigationDelegateProtocol {
                @ObjCSignatureOverride
                override fun webView(
                    webView: WKWebView,
                    didStartProvisionalNavigation: WKNavigation?,
                ) {
                    onCanGoBackChange(webView.canGoBack)
                    onCurrentUrlChange(webView.URL?.absoluteString)
                }

                @ObjCSignatureOverride
                override fun webView(
                    webView: WKWebView,
                    didCommitNavigation: WKNavigation?,
                ) {
                    onCanGoBackChange(webView.canGoBack)
                    onCurrentUrlChange(webView.URL?.absoluteString)
                }

                @ObjCSignatureOverride
                override fun webView(
                    webView: WKWebView,
                    didFinishNavigation: WKNavigation?,
                ) {
                    onCanGoBackChange(webView.canGoBack)
                    onCurrentUrlChange(webView.URL?.absoluteString)
                }
            }
        }

    val webView =
        remember {
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = WKWebViewConfiguration(),
            )
        }

    DisposableEffect(webView, navigationDelegate) {
        webView.navigationDelegate = navigationDelegate
        onDispose {
            webView.navigationDelegate = null
        }
    }

    LaunchedEffect(url) {
        val requestUrl = NSURL(string = url)
        webView.loadRequest(NSURLRequest.requestWithURL(requestUrl))
    }

    LaunchedEffect(goBackTrigger) {
        if (goBackTrigger > 0 && webView.canGoBack) {
            webView.goBack()
            onCanGoBackChange(webView.canGoBack)
        }
    }

    UIKitView(
        modifier = modifier,
        factory = { webView },
        update = {
            onCanGoBackChange(webView.canGoBack)
            onCurrentUrlChange(webView.URL?.absoluteString)
        },
    )
}
