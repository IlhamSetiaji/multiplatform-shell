package org.prasi.sharedModule

import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Android"

@Composable
fun MainWebView() = WebViewApp()
