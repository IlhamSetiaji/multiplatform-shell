package org.prasi.sharedModule

import android.app.Activity
import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Android"

@Composable
fun MainWebView(activity: Activity) = WebViewApp(activity)
