package com.lamontlabs.quantravision.devbot.monitors

import android.content.Context
import android.os.Process
import com.lamontlabs.quantravision.devbot.data.ErrorSeverity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.min

data class LogcatError(
    val message: String,
    val stackTrace: String?,
    val severity: ErrorSeverity,
    val tag: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LogcatMonitor(
    private val context: Context,
    private val maxLinesPerSecond: Int = 100
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val packageName = context.packageName
    private val pid = Process.myPid()
    
    private val _errors = MutableSharedFlow<LogcatError>(
        replay = 50,
        extraBufferCapacity = 500
    )
    val errors: SharedFlow<LogcatError> = _errors.asSharedFlow()
    
    @Volatile
    private var logcatProcess: java.lang.Process? = null
    
    @Volatile
    private var monitoringJob: Job? = null
    
    @Volatile
    private var isMonitoring = false
    
    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = scope.launch {
            var reader: BufferedReader? = null
            try {
                logcatProcess = Runtime.getRuntime().exec(
                    arrayOf(
                        "logcat",
                        "-v", "time",
                        "--pid=$pid",
                        "*:W"
                    )
                )
                
                reader = BufferedReader(
                    InputStreamReader(logcatProcess!!.inputStream),
                    8192
                )
                
                var currentError: StringBuilder? = null
                var currentTag = ""
                var currentSeverity = ErrorSeverity.MEDIUM
                
                var linesProcessed = 0
                var windowStartTime = System.currentTimeMillis()
                
                while (isActive && isMonitoring) {
                    val line = reader.readLine() ?: break
                    
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - windowStartTime >= 1000) {
                        linesProcessed = 0
                        windowStartTime = currentTime
                    }
                    
                    if (linesProcessed >= maxLinesPerSecond) {
                        delay(min(50, 1000 - (currentTime - windowStartTime)))
                        continue
                    }
                    
                    linesProcessed++
                    
                    when {
                        line.contains("E/") -> {
                            emitCurrentError(currentError, currentTag, currentSeverity)
                            currentError = StringBuilder(line)
                            currentTag = extractTag(line)
                            currentSeverity = ErrorSeverity.HIGH
                        }
                        line.contains("W/") -> {
                            emitCurrentError(currentError, currentTag, currentSeverity)
                            currentError = StringBuilder(line)
                            currentTag = extractTag(line)
                            currentSeverity = ErrorSeverity.MEDIUM
                        }
                        line.contains("F/") || line.contains("A/") -> {
                            emitCurrentError(currentError, currentTag, currentSeverity)
                            currentError = StringBuilder(line)
                            currentTag = extractTag(line)
                            currentSeverity = ErrorSeverity.CRITICAL
                        }
                        currentError != null -> {
                            if (currentError.length < 10000) {
                                currentError.append("\n").append(line)
                            }
                        }
                    }
                }
                
                emitCurrentError(currentError, currentTag, currentSeverity)
                
            } catch (e: Exception) {
                if (isMonitoring) {
                    e.printStackTrace()
                }
            } finally {
                try {
                    reader?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                cleanupProcess()
            }
        }
    }
    
    private fun cleanupProcess() {
        try {
            logcatProcess?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            logcatProcess = null
        }
    }
    
    private suspend fun emitCurrentError(
        errorBuilder: StringBuilder?,
        tag: String,
        severity: ErrorSeverity
    ) {
        if (errorBuilder != null && errorBuilder.isNotEmpty()) {
            val fullMessage = errorBuilder.toString()
            val (message, stackTrace) = parseErrorMessage(fullMessage)
            
            _errors.emit(
                LogcatError(
                    message = message,
                    stackTrace = stackTrace,
                    severity = severity,
                    tag = tag
                )
            )
        }
    }
    
    private fun extractTag(line: String): String {
        val tagRegex = Regex("[VDIWEFA]/([^:]+):")
        return tagRegex.find(line)?.groupValues?.get(1) ?: "Unknown"
    }
    
    private fun parseErrorMessage(fullMessage: String): Pair<String, String?> {
        val lines = fullMessage.split("\n")
        val firstLine = lines.firstOrNull() ?: return "" to null
        
        val messageStart = firstLine.indexOf(": ")
        val message = if (messageStart >= 0) {
            firstLine.substring(messageStart + 2)
        } else {
            firstLine
        }
        
        val stackTrace = if (lines.size > 1) {
            lines.drop(1).joinToString("\n")
        } else {
            null
        }
        
        return message to stackTrace
    }
    
    fun stop() {
        if (!isMonitoring) return
        
        isMonitoring = false
        
        monitoringJob?.cancel()
        monitoringJob = null
        
        cleanupProcess()
    }
}
