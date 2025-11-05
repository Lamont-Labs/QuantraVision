package com.lamontlabs.quantravision

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.jvm.Volatile

class CrashLogger(private val context: Context) : Thread.UncaughtExceptionHandler {
  
  private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
  
  companion object {
    private const val TAG = "CrashLogger"
    private const val CRASH_LOG_FILENAME = "quantravision_crash.log"
    
    @Volatile
    private var instance: CrashLogger? = null
    
    fun initialize(context: Context) {
      if (instance == null) {
        synchronized(this) {
          if (instance == null) {
            instance = CrashLogger(context.applicationContext)
            Thread.setDefaultUncaughtExceptionHandler(instance)
            Log.d(TAG, "CrashLogger initialized")
          }
        }
      }
    }
    
    fun getCrashLogFile(context: Context): File {
      return File(context.filesDir, CRASH_LOG_FILENAME)
    }
    
    fun readCrashLog(context: Context): String {
      val file = getCrashLogFile(context)
      return if (file.exists()) {
        file.readText()
      } else {
        "No crash logs found"
      }
    }
    
    fun clearCrashLog(context: Context) {
      val file = getCrashLogFile(context)
      if (file.exists()) {
        file.delete()
      }
    }
  }
  
  override fun uncaughtException(thread: Thread, throwable: Throwable) {
    try {
      saveCrashLog(thread, throwable)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to save crash log", e)
    }
    
    defaultHandler?.uncaughtException(thread, throwable)
  }
  
  private fun saveCrashLog(thread: Thread, throwable: Throwable) {
    val logFile = getCrashLogFile(context)
    
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    
    val deviceInfo = buildString {
      appendLine("=" .repeat(60))
      appendLine("QUANTRAVISION CRASH REPORT")
      appendLine("=" .repeat(60))
      appendLine("Timestamp: $timestamp")
      appendLine("App Version: ${getAppVersion()}")
      appendLine()
      appendLine("DEVICE INFO:")
      appendLine("  Manufacturer: ${Build.MANUFACTURER}")
      appendLine("  Model: ${Build.MODEL}")
      appendLine("  Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
      appendLine("  Device: ${Build.DEVICE}")
      appendLine("  Product: ${Build.PRODUCT}")
      appendLine()
      appendLine("MEMORY INFO:")
      val runtime = Runtime.getRuntime()
      appendLine("  Max Memory: ${runtime.maxMemory() / 1024 / 1024} MB")
      appendLine("  Total Memory: ${runtime.totalMemory() / 1024 / 1024} MB")
      appendLine("  Free Memory: ${runtime.freeMemory() / 1024 / 1024} MB")
      appendLine()
      appendLine("CRASH DETAILS:")
      appendLine("  Thread: ${thread.name}")
      appendLine("  Exception: ${throwable.javaClass.simpleName}")
      appendLine("  Message: ${throwable.message}")
      appendLine()
      appendLine("STACK TRACE:")
    }
    
    val stackTrace = StringWriter().apply {
      throwable.printStackTrace(PrintWriter(this))
    }.toString()
    
    val fullLog = deviceInfo + stackTrace + "\n" + "=" .repeat(60) + "\n\n"
    
    logFile.appendText(fullLog)
    
    Log.e(TAG, "Crash log saved to: ${logFile.absolutePath}")
  }
  
  private fun getAppVersion(): String {
    return try {
      val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
      "${packageInfo.versionName} (${packageInfo.longVersionCode})"
    } catch (e: Exception) {
      "Unknown"
    }
  }
}
