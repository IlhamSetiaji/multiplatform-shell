package org.prasi.shell.settings

import androidx.compose.ui.graphics.Color
import org.prasi.shell.utils.KLogSeverity
import org.prasi.shell.utils.KLogger

class WebSettings {

    var isJavaScriptEnabled = true

    var customUserAgentString: String? = null

    var zoomLevel: Double = 1.0

    var supportZoom: Boolean = true

    var allowFileAccessFromFileURLs: Boolean = false

    var allowUniversalAccessFromFileURLs: Boolean = false

    var logSeverity: KLogSeverity = KLogSeverity.Info
        set(value) {
            field = value
            KLogger.setMinSeverity(value)
        }

    var backgroundColor = Color.Transparent

    val androidWebSettings = PlatformWebSettings.AndroidWebSettings()

    val desktopWebSettings = PlatformWebSettings.DesktopWebSettings()

    val iOSWebSettings = PlatformWebSettings.IOSWebSettings()
}