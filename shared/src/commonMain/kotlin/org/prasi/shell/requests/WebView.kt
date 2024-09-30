package org.prasi.shell.requests

data class WebRequest(
    val url: String,
    val headers: MutableMap<String, String> = mutableMapOf(),
    val isForMainFrame: Boolean = false,
    val isRedirect: Boolean = false,
    val method: String = "GET",
)