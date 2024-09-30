package org.prasi.shell.views

import org.prasi.shell.bridges.WebViewBridge
import org.prasi.shell.utils.KLogger
import kmmshell.shared.generated.resources.Res
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.ExperimentalResourceApi

expect class NativeWebView

interface WebViewInterface {

    val webView: NativeWebView

    val scope: CoroutineScope

    val webViewBridge: WebViewBridge?

    fun canGoBack(): Boolean

    fun canGoForward(): Boolean

    fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String> = emptyMap(),
    )

    fun loadHtml(
        html: String? = null,
        baseUrl: String? = null,
        mimeType: String? = "text/html",
        encoding: String? = "utf-8",
        historyUrl: String? = null,
    )

    suspend fun loadContent(content: WebContent) {
        when (content) {
            is WebContent.Url ->
                loadUrl(
                    content.url,
                    content.additionalHttpHeaders,
                )

            is WebContent.Data ->
                loadHtml(
                    content.data,
                    content.baseUrl,
                    content.mimeType,
                    content.encoding,
                    content.historyUrl,
                )

            is WebContent.File ->
                loadHtmlFile(
                    content.fileName,
                )

            is WebContent.Post ->
                postUrl(
                    content.url,
                    content.postData,
                )

            is WebContent.NavigatorOnly -> {}
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadRawHtmlFile(fileName: String) {
        val html = Res.readBytes(fileName).decodeToString().trimIndent()
        loadHtml(html, encoding = "utf-8")
    }

    suspend fun loadHtmlFile(fileName: String)

    fun postUrl(
        url: String,
        postData: ByteArray,
    )

    fun goBack()

    fun goForward()

    fun reload()

    fun stopLoading()

    fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)? = null,
    )

    fun injectJsBridge() {
        if (webViewBridge == null) return
        val jsBridgeName = webViewBridge!!.jsBridgeName
        KLogger.d {
            "IWebView injectJsBridge"
        }
        val initJs =
            """
            window.$jsBridgeName = {
                callbacks: {},
                callbackId: 0,
                callNative: function (methodName, params, callback) {
                    var message = {
                        methodName: methodName,
                        params: params,
                        callbackId: callback ? window.$jsBridgeName.callbackId++ : -1
                    };
                    if (callback) {
                        window.$jsBridgeName.callbacks[message.callbackId] = callback;
                        console.log('add callback: ' + message.callbackId + ', ' + callback);
                    }
                    window.$jsBridgeName.postMessage(JSON.stringify(message));
                },
                onCallback: function (callbackId, data) {
                    var callback = window.$jsBridgeName.callbacks[callbackId];
                    console.log('onCallback: ' + callbackId + ', ' + data + ', ' + callback);
                    if (callback) {
                        callback(data);
                        delete window.$jsBridgeName.callbacks[callbackId];
                    }
                }
            };
            """.trimIndent()
        evaluateJavaScript(initJs)
    }

    fun initJsBridge(webViewBridge: WebViewBridge)

    fun initWebView() {
        webViewBridge?.apply {
            initJsBridge(this)
        }
    }

    fun saveState(): WebViewBundle?

    fun scrollOffset(): Pair<Int, Int>
}