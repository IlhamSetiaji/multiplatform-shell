package org.prasi.shell.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import org.prasi.shell.bridges.WebViewBridge
import org.prasi.shell.utils.KLogger
import org.prasi.shell.utils.getPlatform
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge

@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewBridge: WebViewBridge? = null,
    onCreated: () -> Unit = {},
    onDispose: () -> Unit = {},
) {
    WebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        webViewBridge = webViewBridge,
        onCreated = { _ -> onCreated() },
        onDispose = { _ -> onDispose() },
    )
}

@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewBridge: WebViewBridge? = null,
    onCreated: (NativeWebView) -> Unit = {},
    onDispose: (NativeWebView) -> Unit = {},
    factory: ((WebViewFactoryParam) -> NativeWebView)? = null,
) {
    val webView = state.webView

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            with(navigator) {
                KLogger.d {
                    "wv.handleNavigationEvents()"
                }
                wv.handleNavigationEvents()
            }
        }

        if (!getPlatform().isDesktop()) {
            LaunchedEffect(wv, state) {
                snapshotFlow { state.content }.collect { content ->
                    when (content) {
                        is WebContent.Url -> {
                            state.lastLoadedUrl = content.url
                            wv.loadUrl(content.url, content.additionalHttpHeaders)
                        }

                        is WebContent.Data -> {
                            wv.loadHtml(
                                content.data,
                                content.baseUrl,
                                content.mimeType,
                                content.encoding,
                                content.historyUrl,
                            )
                        }

                        is WebContent.File -> {
                            wv.loadHtmlFile(content.fileName)
                        }

                        is WebContent.Post -> {
                            wv.postUrl(
                                content.url,
                                content.postData,
                            )
                        }

                        is WebContent.NavigatorOnly -> {
                            // No operation
                        }
                    }
                }
            }
        }

        if (webViewBridge != null && !getPlatform().isDesktop()) {
            LaunchedEffect(wv, state) {
                val loadingStateFlow =
                    snapshotFlow { state.loadingState }.filter { it is LoadingState.Finished }
                val lastLoadedUrFlow =
                    snapshotFlow { state.lastLoadedUrl }.filter { !it.isNullOrEmpty() }

                // Only inject the js bridge when url is changed and the loading state is finished.
                merge(loadingStateFlow, lastLoadedUrFlow).collect {
                    // double check the loading state to make sure the WebView is loaded.
                    if (state.loadingState is LoadingState.Finished) {
                        wv.injectJsBridge()
                    }
                }
            }
        }
    }

    ActualWebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        webViewBridge = webViewBridge,
        onCreated = onCreated,
        onDispose = onDispose,
        factory = factory ?: ::defaultWebViewFactory,
    )

    DisposableEffect(Unit) {
        onDispose {
            KLogger.d {
                "WebView DisposableEffect"
            }
            webViewBridge?.clear()
        }
    }
}

expect class WebViewFactoryParam

expect fun defaultWebViewFactory(param: WebViewFactoryParam): NativeWebView

@Composable
expect fun ActualWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewBridge: WebViewBridge? = null,
    onCreated: (NativeWebView) -> Unit = {},
    onDispose: (NativeWebView) -> Unit = {},
    factory: (WebViewFactoryParam) -> NativeWebView = ::defaultWebViewFactory,
)