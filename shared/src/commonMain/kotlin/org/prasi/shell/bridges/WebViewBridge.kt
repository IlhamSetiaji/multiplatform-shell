package org.prasi.shell.bridges

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import org.prasi.shell.views.WebViewInterface
import org.prasi.shell.views.WebViewNavigator

@Immutable
open class WebViewBridge(val navigator: WebViewNavigator? = null, val jsBridgeName: String = "kmpJsBridge") {
    private val messageBridgeDispatcher = MessageBridgeDispatcher()
    private var callbackId: Int? = null
    var webView: WebViewInterface? = null

    fun register(handler: MessageHandlerInterface) {
        messageBridgeDispatcher.registerJSHandler(handler)
    }

    fun unregister(handler: MessageHandlerInterface) {
        messageBridgeDispatcher.unregisterJSHandler(handler)
    }

    fun clear() {
        messageBridgeDispatcher.clear()
    }

    fun dispatch(message: MessageBridge) {
        messageBridgeDispatcher.dispatch(message, navigator) {
            callbackId = message.callbackId
            onCallback(it, message.callbackId)
        }
    }

    fun extractCallbackIdFromData(): Int? {
        return callbackId
    }

    private fun onCallback(
        data: String,
        callbackId: Int,
    ) {
        webView?.evaluateJavaScript("window.$jsBridgeName.onCallback($callbackId, '$data')")
    }
}

@Composable
fun rememberWebViewJsBridge(navigator: WebViewNavigator? = null): WebViewBridge = remember { WebViewBridge(navigator) }
