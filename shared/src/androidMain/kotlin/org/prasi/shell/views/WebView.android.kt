package org.prasi.shell.views

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.prasi.shell.bridges.WebViewBridge

/**
 * Android WebView implementation.
 */
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewBridge: WebViewBridge?,
    onCreated: (NativeWebView) -> Unit,
    onDispose: (NativeWebView) -> Unit,
    factory: (WebViewFactoryParam) -> NativeWebView,
) {
    AccompanistWebView(
        state,
        modifier,
        captureBackPresses,
        navigator,
        webViewBridge,
        onCreated = onCreated,
        onDispose = onDispose,
        factory = { factory(WebViewFactoryParam(it)) },
    )
}

/** Android WebView factory parameters: a context. */
actual data class WebViewFactoryParam(val context: Context)

/** Default WebView factory for Android. */
actual fun defaultWebViewFactory(param: WebViewFactoryParam) = android.webkit.WebView(param.context)