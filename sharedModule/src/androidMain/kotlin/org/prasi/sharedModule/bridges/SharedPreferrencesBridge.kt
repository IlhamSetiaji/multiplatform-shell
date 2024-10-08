package org.prasi.sharedModule.bridges

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import co.touchlab.kermit.Logger
import org.prasi.shell.bridges.MessageBridge
import org.prasi.shell.bridges.MessageHandlerInterface
import org.prasi.shell.views.WebViewNavigator
import java.io.File
import java.io.FileOutputStream

actual class SharedPreferrencesBridge actual constructor(private val activity: Any) : MessageHandlerInterface {

    override fun methodName(): String {
        return "SharedPreferrences"
    }

    override fun handle(
        message: MessageBridge,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        val sharedPreferences: SharedPreferences = (activity as Activity).getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        val base64Image = sharedPreferences.getString("base64Image", null)
        if (base64Image != null) {
            val byteArray = Base64.decode(base64Image, Base64.DEFAULT)
            val imageFile = saveImageToFile(byteArray)
            val imageUri = Uri.fromFile(imageFile)
            Logger.i("Image URI: $imageUri")
            callback(imageUri.toString())
        } else {
            Logger.i { "Image not found" }
        }
    }

    private fun saveImageToFile(byteArray: ByteArray): File {
        val imageFile = File((activity as Activity).cacheDir, "image.jpg")
        FileOutputStream(imageFile).use { it.write(byteArray) }
        return imageFile
    }
}