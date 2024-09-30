package org.prasi.shell.bridges

import kotlinx.serialization.Serializable

@Serializable
data class MessageBridge(
    val callbackId: Int,
    val methodName: String,
    val params: String,
)