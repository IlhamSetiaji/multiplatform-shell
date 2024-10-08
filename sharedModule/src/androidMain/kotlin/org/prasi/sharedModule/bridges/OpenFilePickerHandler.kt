package org.prasi.sharedModule.bridges

import android.app.Activity
import android.content.Intent
import org.prasi.shell.bridges.MessageBridge
import org.prasi.shell.bridges.MessageHandlerInterface
import org.prasi.shell.views.WebViewNavigator

actual class OpenFilePickerHandler actual constructor(private val activity: Any) : MessageHandlerInterface{
    override fun methodName(): String {
        return "OpenFilePicker"
    }

    override fun handle(message: MessageBridge, navigator: WebViewNavigator?, callback: (String) -> Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
        }
        (activity as Activity).startActivityForResult(intent, REQUEST_FILE_PICKER)
        callback("File Picker opened")
    }

    companion object {
        const val REQUEST_FILE_PICKER = 1002
    }
}