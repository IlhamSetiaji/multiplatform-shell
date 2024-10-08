package org.prasi.sharedModule.bridges

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.prasi.sharedModule.eventbus.FlowEventBus
import org.prasi.sharedModule.eventbus.NavigationEvent
import org.prasi.shell.bridges.MessageBridge
import org.prasi.shell.bridges.MessageHandlerInterface
import org.prasi.shell.views.WebViewNavigator
import java.io.ByteArrayOutputStream

actual class OpenCameraHandler actual constructor(private val activity: Any) : MessageHandlerInterface {

    private var callback: ((String) -> Unit)? = null

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
        this.callback = callback
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                val base64Image = encodeImageToBase64(it)
                saveImageToLocalStorage(base64Image)
                callback?.invoke(base64Image)
            }
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveImageToLocalStorage(base64Image: String) {
        val sharedPreferences: SharedPreferences = (activity as Activity).getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("base64Image", base64Image)
        editor.apply()
    }

    fun getImageFromLocalStorage(): String? {
        val sharedPreferences: SharedPreferences = (activity as Activity).getSharedPreferences("local_storage", Context.MODE_PRIVATE)
        return sharedPreferences.getString("base64Image", null)
    }

    companion object {
        const val REQUEST_CAMERA = 1001
    }
}