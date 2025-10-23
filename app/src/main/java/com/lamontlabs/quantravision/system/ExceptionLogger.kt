package com.lamontlabs.quantravision.system

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * ExceptionLogger
 * Installs an UncaughtExceptionHandler that writes to /files/crash_logs/.
 * Use to show CrashRecoveryDialog on next launch.
 */
object ExceptionLogger {

    private const val DIR = "crash_logs"

    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val dir = File(context.filesDir, DIR).apply { mkdirs() }
                val f = File(dir, "crash_${System.currentTimeMillis()}.log")
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                f.writeText(sw.toString())
            } catch (_: Exception) { /* ignore */ }
            previous?.uncaughtException(t, e)
        }
    }

    fun hasCrashLog(context: Context): Boolean {
        val dir = File(context.filesDir, DIR)
        return dir.exists() && (dir.listFiles()?.isNotEmpty() == true)
    }

    fun clear(context: Context) {
        val dir = File(context.filesDir, DIR)
        dir.listFiles()?.forEach { it.delete() }
    }
}
