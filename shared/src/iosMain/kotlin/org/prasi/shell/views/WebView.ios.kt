package org.prasi.shell.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import org.prasi.shell.bridges.WebViewBridge
import platform.CoreGraphics.CGRectZero
import platform.Foundation.setValue
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled

/**
 * iOS WebView implementation.
 */
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: (NativeWebView) -> Unit,
    onDispose: (NativeWebView) -> Unit,
    factory: (WebViewFactoryParam) -> NativeWebView,
) {
    IOSWebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        webViewJsBridge = webViewJsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
        factory = factory,
    )
}

/** iOS WebView factory parameters: configuration created from WebSettings. */
actual data class WebViewFactoryParam(val config: WKWebViewConfiguration)

/** Default WebView factory for iOS. */
@OptIn(ExperimentalForeignApi::class)
actual fun defaultWebViewFactory(param: WebViewFactoryParam) = WKWebView(frame = CGRectZero.readValue(), configuration = param.config)

/**
 * iOS WebView implementation.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun IOSWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: (NativeWebView) -> Unit,
    onDispose: (NativeWebView) -> Unit,
    factory: (WebViewFactoryParam) -> NativeWebView,
) {
    val observer =
        remember {
            WKWebViewObserver(
                state = state,
                navigator = navigator,
            )
        }
    val navigationDelegate = remember { WKNavigationDelegate(state, navigator) }
    val scope = rememberCoroutineScope()

    UIKitView(
        factory = {
            val config =
                WKWebViewConfiguration().apply {
                    allowsInlineMediaPlayback = true
                    defaultWebpagePreferences.allowsContentJavaScript =
                        state.webSettings.isJavaScriptEnabled
                    preferences.apply {
                        setValue(
                            state.webSettings.allowFileAccessFromFileURLs,
                            forKey = "allowFileAccessFromFileURLs",
                        )
                        javaScriptEnabled = state.webSettings.isJavaScriptEnabled
                    }
                    setValue(
                        state.webSettings.allowUniversalAccessFromFileURLs,
                        forKey = "allowUniversalAccessFromFileURLs",
                    )
                }
            factory(WebViewFactoryParam(config)).apply {
                onCreated(this)
                state.viewState?.let {
                    this.interactionState = it
                }
                allowsBackForwardNavigationGestures = captureBackPresses
                customUserAgent = state.webSettings.customUserAgentString
                this.addProgressObservers(
                    observer = observer,
                )
                this.navigationDelegate = navigationDelegate

                state.webSettings.let {
                    val backgroundColor =
                        (it.iOSWebSettings.backgroundColor ?: it.backgroundColor).toUIColor()
                    val scrollViewColor =
                        (
                                it.iOSWebSettings.underPageBackgroundColor
                                    ?: it.backgroundColor
                                ).toUIColor()
                    setOpaque(it.iOSWebSettings.opaque)
                    if (!it.iOSWebSettings.opaque) {
                        setBackgroundColor(backgroundColor)
                        scrollView.setBackgroundColor(scrollViewColor)
                    }
                    scrollView.pinchGestureRecognizer?.enabled = it.supportZoom
                }
                state.webSettings.iOSWebSettings.let {
                    with(scrollView) {
                        bounces = it.bounces
                        scrollEnabled = it.scrollEnabled
                        showsHorizontalScrollIndicator = it.showHorizontalScrollIndicator
                        showsVerticalScrollIndicator = it.showVerticalScrollIndicator
                    }
                }
            }.also {
                val iosWebView = IOSWebView(it, scope, webViewJsBridge)
                state.webView = iosWebView
                webViewJsBridge?.webView = iosWebView
            }
        },
        modifier = modifier,
        onRelease = {
            state.webView = null
            it.removeProgressObservers(
                observer = observer,
            )
            it.navigationDelegate = null
            onDispose(it)
        },
        properties =
        UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

/**
 * Platform specific default WebView factory function. This can be called from
 * a custom factory function for any platforms that don't need to be customized.
 */
actual fun defaultWebViewFactory(param: WebViewFactoryParam): NativeWebView {
    TODO("Not yet implemented")
}

/**
 * Expect API of [WebView] that is implemented in the platform-specific modules.
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
    factory: (WebViewFactoryParam) -> NativeWebView
) {
}