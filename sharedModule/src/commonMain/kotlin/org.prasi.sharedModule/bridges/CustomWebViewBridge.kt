package org.prasi.sharedModule.bridges

import org.prasi.shell.bridges.WebViewBridge

class CustomWebViewBridge : WebViewBridge() {
    init {
        register(GreetMessageHandler())
    }
}
