package org.prasi.sharedModule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import co.touchlab.kermit.Logger
import org.prasi.shell.cookies.Cookie
import org.prasi.shell.utils.KLogSeverity
import org.prasi.shell.views.LoadingState
import org.prasi.shell.views.WebView
import org.prasi.shell.views.WebViewState
import org.prasi.shell.views.rememberWebViewNavigator
import org.prasi.shell.views.rememberWebViewState
import kotlinx.coroutines.flow.filter
import org.prasi.sharedModule.bridges.GreetMessageHandler
import org.prasi.sharedModule.bridges.OpenCameraHandler
import org.prasi.sharedModule.bridges.OpenFilePickerHandler
import org.prasi.sharedModule.bridges.SharedPreferrencesBridge
import org.prasi.shell.bridges.rememberWebViewJsBridge

@Composable
internal fun BasicWebViewSample(navHostController: NavHostController? = null, activity: Any) {
    val initialUrl = "https://eam.avolut.com"
//    val initialUrl = "https://wareify.avolut.com/"
    val state = rememberWebViewState(url = initialUrl)
    DisposableEffect(Unit) {
        state.webSettings.apply {
            logSeverity = KLogSeverity.Debug
            customUserAgentString =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1) AppleWebKit/625.20 (KHTML, like Gecko) Version/14.3.43 Safari/625.20"
        }

        onDispose { }
    }
    val navigator = rememberWebViewNavigator()
    var textFieldValue by remember(state.lastLoadedUrl) {
        mutableStateOf(state.lastLoadedUrl)
    }

    val webViewBridge = rememberWebViewJsBridge(navigator)

    webViewBridge.register(GreetMessageHandler())
    webViewBridge.register(OpenCameraHandler(activity))
    webViewBridge.register(SharedPreferrencesBridge(activity))
    webViewBridge.register(OpenFilePickerHandler(activity))
    MaterialTheme {
        Column {
            WebView(
                state = state,
                modifier =
                Modifier
                    .fillMaxSize(),
                navigator = navigator,
                webViewBridge = webViewBridge,
            )
        }
    }
}

@Composable
internal fun CookieSample(state: WebViewState) {
    LaunchedEffect(state) {
        snapshotFlow { state.loadingState }
            .filter { it is LoadingState.Finished }
            .collect {
                state.cookieManager.setCookie(
                    "https://github.com",
                    Cookie(
                        name = "test",
                        value = "value",
                        domain = "github.com",
                        expiresDate = 1896863778,
                    ),
                )
                Logger.i {
                    "cookie: ${state.cookieManager.getCookies("https://github.com")}"
                }
                state.cookieManager.removeAllCookies()
                Logger.i {
                    "cookie: ${state.cookieManager.getCookies("https://github.com")}"
                }
            }
    }
}