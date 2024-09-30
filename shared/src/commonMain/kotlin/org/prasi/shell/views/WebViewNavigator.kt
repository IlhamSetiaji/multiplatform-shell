package org.prasi.shell.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.prasi.shell.requests.RequestInterceptorInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
class WebViewNavigator(val coroutineScope: CoroutineScope, val requestInterceptor: RequestInterceptorInterface? = null) {

    private sealed interface NavigationEvent {

        data object Back : NavigationEvent

        data object Forward : NavigationEvent

        data object Reload : NavigationEvent

        data object StopLoading : NavigationEvent

        data class LoadUrl(
            val url: String,
            val additionalHttpHeaders: Map<String, String> = emptyMap(),
        ) : NavigationEvent

        data class LoadHtml(
            val html: String,
            val baseUrl: String? = null,
            val mimeType: String? = null,
            val encoding: String? = "utf-8",
            val historyUrl: String? = null,
        ) : NavigationEvent

        data class LoadHtmlFile(
            val fileName: String,
        ) : NavigationEvent

        data class PostUrl(
            val url: String,
            val postData: ByteArray,
        ) : NavigationEvent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as PostUrl

                if (url != other.url) return false
                if (!postData.contentEquals(other.postData)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = url.hashCode()
                result = 31 * result + postData.contentHashCode()
                return result
            }
        }

        data class EvaluateJavaScript(
            val script: String,
            val callback: ((String) -> Unit)?,
        ) : NavigationEvent
    }

    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow(replay = 1)

    internal suspend fun WebViewInterface.handleNavigationEvents(): Nothing =
        withContext(Dispatchers.Main) {
            navigationEvents.collect { event ->
                when (event) {
                    is NavigationEvent.Back -> goBack()
                    is NavigationEvent.Forward -> goForward()
                    is NavigationEvent.Reload -> reload()
                    is NavigationEvent.StopLoading -> stopLoading()
                    is NavigationEvent.LoadHtml ->
                        loadHtml(
                            event.html,
                            event.baseUrl,
                            event.mimeType,
                            event.encoding,
                            event.historyUrl,
                        )

                    is NavigationEvent.LoadHtmlFile -> {
                        loadHtmlFile(event.fileName)
                    }

                    is NavigationEvent.LoadUrl -> {
                        loadUrl(event.url, event.additionalHttpHeaders)
                    }

                    is NavigationEvent.PostUrl -> {
                        postUrl(event.url, event.postData)
                    }

                    is NavigationEvent.EvaluateJavaScript -> {
                        evaluateJavaScript(event.script, event.callback)
                    }
                }
            }
        }

    var canGoBack: Boolean by mutableStateOf(false)
        internal set

    var canGoForward: Boolean by mutableStateOf(false)
        internal set

    fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadUrl(
                    url,
                    additionalHttpHeaders,
                ),
            )
        }
    }

    fun loadHtml(
        html: String,
        baseUrl: String? = null,
        mimeType: String? = null,
        encoding: String? = "utf-8",
        historyUrl: String? = null,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadHtml(
                    html,
                    baseUrl,
                    mimeType,
                    encoding,
                    historyUrl,
                ),
            )
        }
    }

    fun loadHtmlFile(fileName: String) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadHtmlFile(
                    fileName,
                ),
            )
        }
    }

    fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.PostUrl(
                    url,
                    postData,
                ),
            )
        }
    }

    fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)? = null,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.EvaluateJavaScript(
                    script,
                    callback,
                ),
            )
        }
    }

    fun navigateBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Back) }
    }

    fun navigateForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Forward) }
    }

    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Reload) }
    }

    fun stopLoading() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.StopLoading) }
    }
}

@Composable
fun rememberWebViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    requestInterceptor: RequestInterceptorInterface? = null,
): WebViewNavigator = remember(coroutineScope) { WebViewNavigator(coroutineScope, requestInterceptor) }