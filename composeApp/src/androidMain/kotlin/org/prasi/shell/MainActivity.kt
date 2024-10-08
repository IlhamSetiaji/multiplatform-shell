package org.prasi.shell

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import org.prasi.sharedModule.MainWebView
import org.prasi.sharedModule.bridges.OpenCameraHandler

class MainActivity : AppCompatActivity() {

    private lateinit var openCameraHandler: OpenCameraHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        openCameraHandler = OpenCameraHandler(this)
        setContent {
            MainWebView(activity = this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        openCameraHandler.handleActivityResult(requestCode, resultCode, data)
    }
}
