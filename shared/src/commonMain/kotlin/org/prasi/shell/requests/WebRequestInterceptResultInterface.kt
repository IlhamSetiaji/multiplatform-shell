package org.prasi.shell.requests

sealed interface WebRequestInterceptResultInterface {
    data object Allow : WebRequestInterceptResultInterface

    data object Reject : WebRequestInterceptResultInterface

    class Modify(val request: WebRequest) : WebRequestInterceptResultInterface
}