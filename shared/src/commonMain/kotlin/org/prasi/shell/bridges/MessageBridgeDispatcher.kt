package org.prasi.shell.bridges

import androidx.compose.runtime.Immutable
import org.prasi.shell.views.WebViewNavigator

@Immutable
internal class MessageBridgeDispatcher {
    private val messageHandlerMap = mutableMapOf<String, MessageHandlerInterface>()

    fun registerJSHandler(handler: MessageHandlerInterface) {
        messageHandlerMap[handler.methodName()] = handler
    }

    fun dispatch(
        message: MessageBridge,
        navigator: WebViewNavigator? = null,
        callback: (String) -> Unit,
    ) {
        messageHandlerMap[message.methodName]?.handle(message, navigator, callback)
    }

    fun canHandle(id: String) = messageHandlerMap.containsKey(id)

    fun unregisterJSHandler(handler: MessageHandlerInterface) {
        messageHandlerMap.remove(handler.methodName())
    }

    fun clear() {
        messageHandlerMap.clear()
    }
}