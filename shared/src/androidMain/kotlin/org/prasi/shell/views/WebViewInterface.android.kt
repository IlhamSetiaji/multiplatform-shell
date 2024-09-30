package org.prasi.shell.views

import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.prasi.shell.bridges.MessageBridge
import org.prasi.shell.bridges.WebViewBridge
import org.prasi.shell.utils.KLogger

actual typealias NativeWebView = WebView

class AndroidWebView(
    override val webView: WebView,
    override val scope: CoroutineScope,
    override val webViewBridge: WebViewBridge?,
) : WebViewInterface {
    init {
        initWebView()
    }

    override fun canGoBack() = webView.canGoBack()

    override fun canGoForward() = webView.canGoForward()

    override fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String>,
    ) {
        webView.loadUrl(url, additionalHttpHeaders)
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?,
    ) {
        if (html == null) return
        webView.loadDataWithBaseURL(baseUrl, html, mimeType, encoding, historyUrl)
    }

    override suspend fun loadHtmlFile(fileName: String) {
        KLogger.d {
            "loadHtmlFile: $fileName"
        }
        webView.loadUrl("file:///android_asset/$fileName")
    }

    override fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        webView.postUrl(url, postData)
    }

    override fun goBack() {
        webView.goBack()
    }

    override fun goForward() {
        webView.goForward()
    }

    override fun reload() {
        webView.reload()
    }

    override fun stopLoading() {
        webView.stopLoading()
    }

    override fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)?,
    ) {
        val androidScript = "javascript:$script"
        KLogger.d {
            "evaluateJavaScript: $androidScript"
        }
        webView.post {
            webView.evaluateJavascript(androidScript, callback)
        }
    }

    override fun injectJsBridge() {
        if (webViewBridge == null) return
        super.injectJsBridge()
        val callAndroid =
            """
            window.${webViewBridge.jsBridgeName}.postMessage = function (message) {
                    window.androidJsBridge.call(message)
                };
            """.trimIndent()
        evaluateJavaScript(callAndroid)
    }

    override fun initJsBridge(webViewBridge: WebViewBridge) {
        webView.addJavascriptInterface(this, "androidJsBridge")
    }

    @JavascriptInterface
    fun call(request: String) {
        KLogger.d { "call from JS: $request" }
        val message = Json.decodeFromString<MessageBridge>(request)
        KLogger.d {
            "call from JS: $message"
        }
        webViewBridge?.dispatch(message)
    }

    @JavascriptInterface
    fun callAndroid(
        id: Int,
        method: String,
        params: String,
    ) {
        KLogger.d { "callAndroid call from JS: $id, $method, $params" }
        webViewBridge?.dispatch(MessageBridge(id, method, params))
    }

    override fun scrollOffset(): Pair<Int, Int> {
        return Pair(webView.scrollX, webView.scrollY)
    }

    override fun saveState(): WebViewBundle? {
        val bundle = WebViewBundle()
        return if (webView.saveState(bundle) != null) {
            bundle
        } else {
            null
        }
    }
}