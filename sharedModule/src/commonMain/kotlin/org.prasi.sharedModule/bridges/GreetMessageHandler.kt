package org.prasi.sharedModule.bridges

import co.touchlab.kermit.Logger
import org.prasi.sharedModule.eventbus.FlowEventBus
import org.prasi.sharedModule.eventbus.NavigationEvent
import org.prasi.sharedModule.models.GreetModel
import org.prasi.shell.bridges.MessageHandlerInterface
import org.prasi.shell.bridges.MessageBridge
import org.prasi.shell.bridges.dataToJsonString
import org.prasi.shell.bridges.processParams
import org.prasi.shell.views.WebViewNavigator
import kotlinx.coroutines.launch

class GreetMessageHandler : MessageHandlerInterface {
    override fun methodName(): String {
        return "Greet"
    }

    override fun handle(
        message: MessageBridge,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        Logger.i {
            "Greet Handler Get Message: $message"
        }
        val param = processParams<GreetModel>(message)
        val data = GreetModel("KMM Received ${param.message}")
        callback(dataToJsonString(data))
//        EventBus.post(NavigationEvent())
        navigator?.coroutineScope?.launch {
            FlowEventBus.publishEvent(NavigationEvent())
        }
    }
}