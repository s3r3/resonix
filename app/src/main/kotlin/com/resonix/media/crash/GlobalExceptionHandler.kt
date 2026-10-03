package com.resonix.media.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val context: Context,
    private val activity: Class<*>,
) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        context.startActivity(
            Intent(context, activity).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("exception", e.stackTraceToString())
            },
        )
        defaultHandler?.uncaughtException(t, e) ?: exitProcess(0)
    }
}

internal fun copyCrashReportToClipboard(clipboard: ClipboardManager, report: String) {
    val marker = "\n\n[Crash report truncated. Use Share for full logs.]"
    val value = if (report.length <= 100_000) report else report.take(100_000 - marker.length) + marker
    clipboard.setPrimaryClip(ClipData.newPlainText(null, value))
}
