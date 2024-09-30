package org.prasi.shell.views

import org.prasi.shell.bridges.MessageBridge
import org.prasi.shell.bridges.WebViewBridge
import org.prasi.shell.utils.KLogger
import dev.datlag.kcef.KCEFBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.network.CefPostData
import org.cef.network.CefPostDataElement
import org.cef.network.CefRequest

actual typealias NativeWebView = KCEFBrowser

class DesktopWebView(
    override val webView: KCEFBrowser,
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
        if (additionalHttpHeaders.isNotEmpty()) {
            val request =
                CefRequest.create().apply {
                    this.url = url
                    this.setHeaderMap(additionalHttpHeaders)
                }
            webView.loadRequest(request)
        } else {
            KLogger.d {
                "DesktopWebView loadUrl $url"
            }
            webView.loadURL(url)
        }
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?,
    ) {
        KLogger.d {
            "DesktopWebView loadHtml"
        }
        if (html != null) {
            webView.loadHtml(html, baseUrl ?: KCEFBrowser.BLANK_URI)
        }
    }

    override suspend fun loadHtmlFile(fileName: String) {
        // TODO
    }

    override fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        val request =
            CefRequest.create().apply {
                this.url = url
                this.postData =
                    CefPostData.create().apply {
                        this.addElement(
                            CefPostDataElement.create().apply {
                                this.setToBytes(postData.size, postData)
                            },
                        )
                    }
            }
        webView.loadRequest(request)
    }

    override fun goBack() = webView.goBack()

    override fun goForward() = webView.goForward()

    override fun reload() = webView.reload()

    override fun stopLoading() = webView.stopLoad()

    override fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)?,
    ) {
        KLogger.d {
            "evaluateJavaScript: $script"
        }
        webView.evaluateJavaScript(script) {
            if (it != null) {
                callback?.invoke(it)
            }
        }
    }

    override fun injectJsBridge() {
        if (webViewBridge == null) return
        super.injectJsBridge()
        KLogger.d {
            "DesktopWebView injectJsBridge"
        }
        val callDesktop =
            """
            window.${webViewBridge.jsBridgeName}.postMessage = function (message) {
                    window.cefQuery({request:message});
                };
            """.trimIndent()
        evaluateJavaScript(callDesktop)
    }

    override fun initJsBridge(webViewBridge: WebViewBridge) {
        KLogger.d {
            "DesktopWebView initJsBridge"
        }
        val router = CefMessageRouter.create()
        val handler =
            object : CefMessageRouterHandlerAdapter() {
                override fun onQuery(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    queryId: Long,
                    request: String?,
                    persistent: Boolean,
                    callback: CefQueryCallback?,
                ): Boolean {
                    if (request == null) {
                        return super.onQuery(
                            browser,
                            frame,
                            queryId,
                            request,
                            persistent,
                            callback,
                        )
                    }
                    val message = Json.decodeFromString<MessageBridge>(request)
                    KLogger.d {
                        "onQuery Message: $message"
                    }
                    webViewBridge.dispatch(message)
                    return true
                }
            }
        router.addHandler(handler, false)
        webView.client.addMessageRouter(router)
    }

    override fun saveState(): WebViewBundle? {
        return null
    }

    override fun scrollOffset(): Pair<Int, Int> {
        return Pair(0, 0)
    }
}