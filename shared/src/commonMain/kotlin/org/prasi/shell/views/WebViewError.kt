package org.prasi.shell.views

import androidx.compose.runtime.Immutable

@Immutable
data class WebViewError(

    val code: Int,

    val description: String,
)