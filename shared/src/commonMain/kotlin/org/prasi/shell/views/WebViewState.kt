package org.prasi.shell.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.prasi.shell.cookies.CookieManager
import org.prasi.shell.cookies.WebViewCookieManager
import org.prasi.shell.settings.WebSettings
import org.prasi.shell.utils.KLogger
import org.prasi.shell.utils.getPlatform
import org.prasi.shell.utils.isZero

class WebViewState(webContent: WebContent) {

    var lastLoadedUrl: String? by mutableStateOf(null)
        internal set

    var content: WebContent by mutableStateOf(webContent)

    var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    val isLoading: Boolean
        get() = loadingState !is LoadingState.Finished

    var pageTitle: String? by mutableStateOf(null)
        internal set

    val errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()

    val webSettings: WebSettings by mutableStateOf(WebSettings())

    internal var webView by mutableStateOf<WebViewInterface?>(null)

    val nativeWebView get() = webView?.webView ?: error("WebView is not initialized")

    var viewState: WebViewBundle? = null
        internal set

    var scrollOffset: Pair<Int, Int> = 0 to 0
        internal set

    val cookieManager: CookieManager by mutableStateOf(WebViewCookieManager())
}

@Composable
fun rememberWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap(),
): WebViewState =
    remember {
        WebViewState(
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders,
            ),
        )
    }.apply {
        this.content =
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders,
            )
    }

@Composable
fun rememberSaveableWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap(),
): WebViewState =
    if (getPlatform().isDesktop()) {
        rememberWebViewState(url, additionalHttpHeaders)
    } else {
        rememberSaveable(saver = WebStateSaver) {
            WebViewState(WebContent.NavigatorOnly)
        }
    }

val WebStateSaver: Saver<WebViewState, Any> =
    run {
        val pageTitleKey = "pagetitle"
        val lastLoadedUrlKey = "lastloaded"
        val stateBundleKey = "bundle"
        val scrollOffsetKey = "scrollOffset"

        mapSaver(
            save = {
                val viewState = it.webView?.saveState()
                KLogger.info {
                    "WebViewStateSaver Save: ${it.pageTitle}, ${it.lastLoadedUrl}, ${it.webView?.scrollOffset()}, $viewState"
                }
                mapOf(
                    pageTitleKey to it.pageTitle,
                    lastLoadedUrlKey to it.lastLoadedUrl,
                    stateBundleKey to viewState,
                    scrollOffsetKey to it.webView?.scrollOffset(),
                )
            },
            restore = {
                KLogger.info {
                    "WebViewStateSaver Restore: ${it[pageTitleKey]}, ${it[lastLoadedUrlKey]}, ${it["scrollOffset"]}, ${it[stateBundleKey]}"
                }
                val scrollOffset = it[scrollOffsetKey] as Pair<Int, Int>? ?: (0 to 0)
                val bundle = it[stateBundleKey] as WebViewBundle?
                WebViewState(WebContent.NavigatorOnly).apply {
                    this.pageTitle = it[pageTitleKey] as String?
                    this.lastLoadedUrl = it[lastLoadedUrlKey] as String?
                    bundle?.let { this.viewState = it }
                    if (!scrollOffset.isZero()) {
                        this.scrollOffset = scrollOffset
                    }
                }
            },
        )
    }

@Composable
fun rememberWebViewStateWithHTMLData(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String? = null,
    historyUrl: String? = null,
): WebViewState =
    remember {
        WebViewState(WebContent.Data(data, baseUrl, encoding, mimeType, historyUrl))
    }.apply {
        this.content =
            WebContent.Data(
                data, baseUrl, encoding, mimeType, historyUrl,
            )
    }

@Composable
fun rememberWebViewStateWithHTMLFile(fileName: String): WebViewState =
    remember {
        WebViewState(WebContent.File(fileName))
    }.apply {
        this.content = WebContent.File(fileName)
    }