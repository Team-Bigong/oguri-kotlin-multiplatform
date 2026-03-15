package com.bigong.oguri.core.ui.component

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PlatformWebView(
    url: String,
    goBackTrigger: Int,
    onCanGoBackChange: (Boolean) -> Unit,
    onCurrentUrlChange: (String?) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val webView =
        remember {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient =
                    object : WebViewClient() {
                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: android.graphics.Bitmap?,
                        ) {
                            super.onPageStarted(view, url, favicon)
                            onCurrentUrlChange(url)
                            onCanGoBackChange(canGoBack())
                        }

                        override fun onPageFinished(
                            view: WebView?,
                            url: String?,
                        ) {
                            super.onPageFinished(view, url)
                            onCanGoBackChange(canGoBack())
                            onCurrentUrlChange(view?.url)
                        }
                    }
            }
        }

    LaunchedEffect(url) {
        if (webView.url != url) {
            webView.loadUrl(url)
        }
    }

    LaunchedEffect(goBackTrigger) {
        if (goBackTrigger > 0 && webView.canGoBack()) {
            webView.goBack()
            onCanGoBackChange(webView.canGoBack())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = { updatedWebView ->
            onCanGoBackChange(updatedWebView.canGoBack())
            onCurrentUrlChange(updatedWebView.url)
        },
    )
}
