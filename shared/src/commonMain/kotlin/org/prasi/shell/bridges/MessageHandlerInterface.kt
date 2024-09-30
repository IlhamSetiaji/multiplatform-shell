package org.prasi.shell.bridges

import org.prasi.shell.views.WebViewNavigator
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface MessageHandlerInterface {

    fun methodName(): String

    fun canHandle(methodName: String) = methodName() == methodName

    fun handle(
        message: MessageBridge,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    )
}

inline fun <reified T : Any> MessageHandlerInterface.processParams(message: MessageBridge): T {
    return Json.decodeFromString(message.params)
}

inline fun <reified T : Any> MessageHandlerInterface.dataToJsonString(res: T): String {
    return Json.encodeToString(res)
}