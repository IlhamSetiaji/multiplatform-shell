package org.prasi.shell.settings

import androidx.compose.ui.graphics.Color

sealed class PlatformWebSettings {

    data class AndroidWebSettings(

        var allowFileAccess: Boolean = false,

        var textZoom: Int = 100,

        var useWideViewPort: Boolean = false,

        var standardFontFamily: String = "sans-serif",

        var defaultFontSize: Int = 16,

        var loadsImagesAutomatically: Boolean = true,

        var isAlgorithmicDarkeningAllowed: Boolean = false,

        var safeBrowsingEnabled: Boolean = true,

        var domStorageEnabled: Boolean = false,

        var mediaPlaybackRequiresUserGesture: Boolean = true,

        var allowProtectedMedia: Boolean = false,

        var allowMidiSysexMessages: Boolean = false,

        var layerType: Int = LayerType.HARDWARE,
    ) : PlatformWebSettings() {
        object LayerType {
            const val NONE = 0
            const val SOFTWARE = 1
            const val HARDWARE = 2
        }
    }


    data class DesktopWebSettings(
        var offScreenRendering: Boolean = false,
        var transparent: Boolean = true,
        var disablePopupWindows: Boolean = false,
    ) : PlatformWebSettings()

    data class IOSWebSettings(

        var opaque: Boolean = false,

        var backgroundColor: Color? = null,

        var underPageBackgroundColor: Color? = null,

        var bounces: Boolean = true,

        var scrollEnabled: Boolean = true,

        var showHorizontalScrollIndicator: Boolean = true,

        var showVerticalScrollIndicator: Boolean = true,
    ) : PlatformWebSettings()
}