package org.prasi.shell.views

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import org.prasi.shell.bridges.WebViewBridge
import org.prasi.shell.requests.WebRequest
import org.prasi.shell.requests.WebRequestInterceptResultInterface
import org.prasi.shell.utils.KLogger

@Composable
fun AccompanistWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewBridge: WebViewBridge? = null,
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
) {
    BoxWithConstraints(modifier) {
        val width =
            if (constraints.hasFixedWidth) {
                ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
        val height =
            if (constraints.hasFixedHeight) {
                ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }

        val layoutParams =
            FrameLayout.LayoutParams(
                width,
                height,
            )

        AccompanistWebView(
            state,
            layoutParams,
            Modifier,
            captureBackPresses,
            navigator,
            webViewBridge,
            onCreated,
            onDispose,
            client,
            chromeClient,
            factory,
        )
    }
}

@Composable
fun AccompanistWebView(
    state: WebViewState,
    layoutParams: FrameLayout.LayoutParams,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewBridge: WebViewBridge? = null,
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
) {
    val webView = state.webView
    val scope = rememberCoroutineScope()

    BackHandler(captureBackPresses && navigator.canGoBack) {
        webView?.goBack()
    }

    client.state = state
    client.navigator = navigator
    chromeClient.state = state

    AndroidView(
        factory = { context ->
            (factory?.invoke(context) ?: WebView(context)).apply {
                onCreated(this)

                this.layoutParams = layoutParams

                state.viewState?.let {
                    this.restoreState(it)
                }

                chromeClient.context = context
                webChromeClient = chromeClient
                webViewClient = client

                this.setLayerType(state.webSettings.androidWebSettings.layerType, null)

                settings.apply {
                    state.webSettings.let {
                        javaScriptEnabled = it.isJavaScriptEnabled
                        domStorageEnabled = it.isDomStorageEnabled
                        userAgentString = it.customUserAgentString
                        allowFileAccessFromFileURLs = it.allowFileAccessFromFileURLs
                        allowUniversalAccessFromFileURLs = it.allowUniversalAccessFromFileURLs
                        setSupportZoom(it.supportZoom)
                    }

                    state.webSettings.androidWebSettings.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            safeBrowsingEnabled = it.safeBrowsingEnabled
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            isAlgorithmicDarkeningAllowed = it.isAlgorithmicDarkeningAllowed
                        }
                        setBackgroundColor(state.webSettings.backgroundColor.toArgb())
                        allowFileAccess = it.allowFileAccess
                        textZoom = it.textZoom
                        useWideViewPort = it.useWideViewPort
                        standardFontFamily = it.standardFontFamily
                        defaultFontSize = it.defaultFontSize
                        loadsImagesAutomatically = it.loadsImagesAutomatically
                        domStorageEnabled = it.domStorageEnabled
                        mediaPlaybackRequiresUserGesture = it.mediaPlaybackRequiresUserGesture
                    }
                }
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    val nightModeFlags =
                        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                        WebSettingsCompat.setForceDark(
                            this.settings,
                            WebSettingsCompat.FORCE_DARK_ON,
                        )
                    } else {
                        WebSettingsCompat.setForceDark(
                            this.settings,
                            WebSettingsCompat.FORCE_DARK_OFF,
                        )
                    }

                    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                        WebSettingsCompat.setForceDarkStrategy(
                            this.settings,
                            WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY,
                        )
                    }
                }
            }.also {
                val androidWebView = AndroidWebView(it, scope, webViewBridge)
                state.webView = androidWebView
                webViewBridge?.webView = androidWebView
            }
        },
        modifier = modifier,
        onReset = {},
        onRelease = {
            onDispose(it)
        },
    )
}

open class AccompanistWebViewClient : WebViewClient() {
    open lateinit var state: WebViewState
        internal set
    open lateinit var navigator: WebViewNavigator
        internal set
    private var isRedirect = false

    override fun onPageStarted(
        view: WebView,
        url: String?,
        favicon: Bitmap?,
    ) {
        super.onPageStarted(view, url, favicon)
        KLogger.d {
            "onPageStarted: $url"
        }
        state.loadingState = LoadingState.Loading(0.0f)
        state.errorsForCurrentRequest.clear()
        state.pageTitle = null
        state.lastLoadedUrl = url

        // set scale level
        @Suppress("ktlint:standard:max-line-length")
        val script =
            "var meta = document.createElement('meta');meta.setAttribute('name', 'viewport');meta.setAttribute('content', 'width=device-width, initial-scale=${state.webSettings.zoomLevel}, maximum-scale=10.0, minimum-scale=0.1,user-scalable=yes');document.getElementsByTagName('head')[0].appendChild(meta);"
        navigator.evaluateJavaScript(script)
    }

    override fun onPageFinished(
        view: WebView,
        url: String?,
    ) {
        super.onPageFinished(view, url)
        KLogger.d {
            "onPageFinished: $url"
        }
        state.loadingState = LoadingState.Finished
        state.lastLoadedUrl = url
    }

    override fun doUpdateVisitedHistory(
        view: WebView,
        url: String?,
        isReload: Boolean,
    ) {
        KLogger.d {
            "doUpdateVisitedHistory: $url"
        }
        super.doUpdateVisitedHistory(view, url, isReload)

        navigator.canGoBack = view.canGoBack()
        navigator.canGoForward = view.canGoForward()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            KLogger.e {
                "onReceivedError: $error"
            }
            return
        }
        KLogger.e {
            "onReceivedError: ${error?.description}"
        }
        if (error != null) {
            state.errorsForCurrentRequest.add(
                WebViewError(
                    error.errorCode,
                    error.description.toString(),
                ),
            )
        }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        KLogger.d {
            "shouldOverrideUrlLoading: ${request?.url} ${request?.isForMainFrame} ${request?.isRedirect} ${request?.method}"
        }
        if (isRedirect || request == null || navigator.requestInterceptor == null) {
            isRedirect = false
            return super.shouldOverrideUrlLoading(view, request)
        }
        val isRedirectRequest =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                request.isRedirect
            } else {
                false
            }
        val webRequest =
            WebRequest(
                request.url.toString(),
                request.requestHeaders?.toMutableMap() ?: mutableMapOf(),
                request.isForMainFrame,
                isRedirectRequest,
                request.method ?: "GET",
            )
        val interceptResult =
            navigator.requestInterceptor!!.onInterceptUrlRequest(
                webRequest,
                navigator,
            )
        return when (interceptResult) {
            is WebRequestInterceptResultInterface.Allow -> {
                false
            }

            is WebRequestInterceptResultInterface.Reject -> {
                true
            }

            is WebRequestInterceptResultInterface.Modify -> {
                isRedirect = true
                interceptResult.request.apply {
                    navigator.stopLoading()
                    navigator.loadUrl(this.url, this.headers)
                }
                true
            }
        }
    }
}


open class AccompanistWebChromeClient : WebChromeClient() {
    open lateinit var state: WebViewState
        internal set
    lateinit var context: Context
        internal set
    private var lastLoadedUrl = ""

    override fun onReceivedTitle(
        view: WebView,
        title: String?,
    ) {
        super.onReceivedTitle(view, title)
        KLogger.d {
            "onReceivedTitle: $title url:${view.url}"
        }
        state.pageTitle = title
        state.lastLoadedUrl = view.url ?: ""
    }

    override fun onReceivedIcon(
        view: WebView,
        icon: Bitmap?,
    ) {
        super.onReceivedIcon(view, icon)
//        state.pageIcon = icon
    }

    override fun onProgressChanged(
        view: WebView,
        newProgress: Int,
    ) {
        super.onProgressChanged(view, newProgress)
        if (state.loadingState is LoadingState.Finished && view.url == lastLoadedUrl) return
        state.loadingState =
            if (newProgress == 100) {
                LoadingState.Finished
            } else {
                LoadingState.Loading(newProgress / 100.0f)
            }
        lastLoadedUrl = view.url ?: ""
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        val grantedPermissions = mutableListOf<String>()
        KLogger.d { "onPermissionRequest received request for resources [${request.resources}]" }

        request.resources.forEach { resource ->
            var androidPermission: String? = null

            when (resource) {
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                    androidPermission = android.Manifest.permission.RECORD_AUDIO
                }

                PermissionRequest.RESOURCE_MIDI_SYSEX -> {
                    // MIDI sysex is only available on Android M and above
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (state.webSettings.androidWebSettings.allowMidiSysexMessages) {
                            grantedPermissions.add(PermissionRequest.RESOURCE_MIDI_SYSEX)
                        }
                    }
                }

                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> {
                    if (state.webSettings.androidWebSettings.allowProtectedMedia) {
                        grantedPermissions.add(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)
                    }
                }

                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                    androidPermission = android.Manifest.permission.CAMERA
                }
            }

            if (androidPermission != null) {
                if (ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(resource)
                    KLogger.d {
                        "onPermissionRequest permission [$androidPermission] was already granted for resource [$resource]"
                    }
                } else {
                    KLogger.w {
                        "onPermissionRequest didn't find already granted permission [$androidPermission] for resource [$resource]"
                    }
                }
            }
        }

        if (grantedPermissions.isNotEmpty()) {
            request.grant(grantedPermissions.toTypedArray())
            KLogger.d { "onPermissionRequest granted permissions: ${grantedPermissions.joinToString()}" }
        } else {
            request.deny()
            KLogger.d { "onPermissionRequest denied permissions: ${request.resources}" }
        }
    }
}