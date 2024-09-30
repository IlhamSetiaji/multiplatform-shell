package org.prasi.sharedModule.navs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.prasi.shell.utils.KLogSeverity
import org.prasi.shell.views.WebView
import org.prasi.shell.views.rememberSaveableWebViewState
import org.prasi.shell.views.rememberWebViewNavigator

object HomeTab : Tab {
    @Composable
    override fun Content() {
        Home()
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "Home"
            val icon = rememberVectorPainter(Icons.Default.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon,
                )
            }
        }
}

@Composable
fun Home() {
    val url = "https://www.jetbrains.com/lp/compose-multiplatform/"
    val webViewState =
        rememberSaveableWebViewState(url).apply {
            webSettings.logSeverity = KLogSeverity.Debug
        }

    val navigator = rememberWebViewNavigator()

    LaunchedEffect(navigator) {
        val bundle = webViewState.viewState
        if (bundle == null) {
            navigator.loadUrl(url)
        }
    }

    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize().padding(bottom = 45.dp),
        navigator = navigator,
    )
}