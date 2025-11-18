package com.lamontlabs.quantravision.devbot.monitors

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.*
import java.io.PrintWriter
import java.io.StringWriter

data class CrashInfo(
    val message: String,
    val throwable: Throwable,
    val threadName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stackTraceString: String
)

class CrashAnalyzer(private val context: Context) {
    private val _crashes = MutableSharedFlow<CrashInfo>(
        replay = 10,
        extraBufferCapacity = 50
    )
    val crashes: SharedFlow<CrashInfo> = _crashes.asSharedFlow()
    
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var isInstalled = false
    
    fun installHandler() {
        if (isInstalled) return
        
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(thread, throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        isInstalled = true
        Log.d("CrashAnalyzer", "Crash handler installed")
    }
    
    fun uninstallHandler() {
        if (!isInstalled) return
        
        defaultHandler?.let {
            Thread.setDefaultUncaughtExceptionHandler(it)
        }
        
        isInstalled = false
    }
    
    private fun handleCrash(thread: Thread, throwable: Throwable) {
        try {
            val stackTraceString = getStackTraceString(throwable)
            
            val crashInfo = CrashInfo(
                message = throwable.message ?: "Unknown crash",
                throwable = throwable,
                threadName = thread.name,
                stackTraceString = stackTraceString
            )
            
            _crashes.tryEmit(crashInfo)
            
            saveCrashToPrefs(crashInfo)
            
            Log.e("CrashAnalyzer", "Crash detected: ${crashInfo.message}", throwable)
            
        } catch (e: Exception) {
            Log.e("CrashAnalyzer", "Error handling crash", e)
        }
    }
    
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }
    
    private fun saveCrashToPrefs(crashInfo: CrashInfo) {
        try {
            val prefs = context.getSharedPreferences("devbot_crashes", Context.MODE_PRIVATE)
            val existingCrashes = prefs.getString("recent_crashes", "") ?: ""
            
            val crashEntry = """
                |[${crashInfo.timestamp}] ${crashInfo.threadName}
                |${crashInfo.message}
                |${crashInfo.stackTraceString}
                |---
            """.trimMargin()
            
            val updatedCrashes = (crashEntry + "\n" + existingCrashes)
                .split("---")
                .take(10)
                .joinToString("---")
            
            prefs.edit()
                .putString("recent_crashes", updatedCrashes)
                .putString("last_crash", crashEntry)
                .putLong("last_crash_time", crashInfo.timestamp)
                .apply()
                
        } catch (e: Exception) {
            Log.e("CrashAnalyzer", "Failed to save crash", e)
        }
    }
    
    fun getLastCrash(): String? {
        return try {
            val prefs = context.getSharedPreferences("devbot_crashes", Context.MODE_PRIVATE)
            prefs.getString("last_crash", null)
        } catch (e: Exception) {
            null
        }
    }
    
    fun clearCrashHistory() {
        try {
            val prefs = context.getSharedPreferences("devbot_crashes", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            Log.e("CrashAnalyzer", "Failed to clear crash history", e)
        }
    }
}
