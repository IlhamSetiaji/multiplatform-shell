package org.prasi.shell.requests

import org.prasi.shell.views.WebViewNavigator

interface RequestInterceptorInterface {
    fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator,
    ): WebRequestInterceptResultInterface
}
