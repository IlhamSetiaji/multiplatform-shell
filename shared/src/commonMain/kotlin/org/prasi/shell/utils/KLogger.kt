package org.prasi.shell.utils

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.mutableLoggerConfigInit
import co.touchlab.kermit.platformLogWriter

internal object KLogger : Logger(
    config = mutableLoggerConfigInit(listOf(platformLogWriter(DefaultFormatter))),
    tag = "ComposeWebView",
) {
    init {
        setMinSeverity(KLogSeverity.Info)
    }

    fun setMinSeverity(severity: KLogSeverity) {
        mutableConfig.minSeverity = severity.toKermitSeverity()
    }

    fun info(msg: () -> String) {
        d { msg() }
    }
}

enum class KLogSeverity {
    Verbose,
    Debug,
    Info,
    Warn,
    Error,
    Assert,
}

fun KLogSeverity.toKermitSeverity(): Severity {
    return when (this) {
        KLogSeverity.Verbose -> Severity.Verbose
        KLogSeverity.Debug -> Severity.Debug
        KLogSeverity.Info -> Severity.Info
        KLogSeverity.Warn -> Severity.Warn
        KLogSeverity.Error -> Severity.Error
        KLogSeverity.Assert -> Severity.Assert
    }
}
