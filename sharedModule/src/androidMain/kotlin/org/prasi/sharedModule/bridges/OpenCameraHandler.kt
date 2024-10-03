// sharedModule/src/androidMain/kotlin/org/prasi/sharedModule/bridges/OpenCameraHandler.kt
package org.prasi.sharedModule.bridges

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.prasi.sharedModule.eventbus.FlowEventBus
import org.prasi.sharedModule.eventbus.NavigationEvent
import org.prasi.shell.bridges.MessageBridge
import org.prasi.shell.bridges.MessageHandlerInterface
import org.prasi.shell.views.WebViewNavigator

actual class OpenCameraHandler actual constructor(private val activity: Any) : MessageHandlerInterface {
    override fun methodName(): String {
        return "OpenCamera"
    }

    override fun handle(
        message: MessageBridge,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        Logger.i {
            "Open Camera Handler Get Message: $message"
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        (activity as Activity).startActivityForResult(intent, REQUEST_CAMERA)
        callback("Camera opened")
        navigator?.coroutineScope?.launch {
            FlowEventBus.publishEvent(NavigationEvent())
        }
    }

    companion object {
        const val REQUEST_CAMERA = 1001
    }
}