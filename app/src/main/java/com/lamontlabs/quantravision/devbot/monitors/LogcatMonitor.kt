package com.lamontlabs.quantravision.devbot.monitors

import android.content.Context
import com.lamontlabs.quantravision.devbot.data.ErrorSeverity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.InputStreamReader

data class LogcatError(
    val message: String,
    val stackTrace: String?,
    val severity: ErrorSeverity,
    val tag: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LogcatMonitor(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val packageName = context.packageName
    
    private val _errors = MutableSharedFlow<LogcatError>(
        replay = 50,
        extraBufferCapacity = 500
    )
    val errors: SharedFlow<LogcatError> = _errors.asSharedFlow()
    
    private var logcatProcess: Process? = null
    private var monitoringJob: Job? = null
    
    init {
        startMonitoring()
    }
    
    private fun startMonitoring() {
        monitoringJob = scope.launch {
            try {
                logcatProcess = Runtime.getRuntime().exec(
                    arrayOf("logcat", "-v", "time", "*:W")
                )
                
                val reader = BufferedReader(
                    InputStreamReader(logcatProcess!!.inputStream)
                )
                
                var currentError: StringBuilder? = null
                var currentTag = ""
                var currentSeverity = ErrorSeverity.MEDIUM
                
                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (!isActive) return@forEach
                        
                        if (line.contains(packageName)) {
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
                                    currentError.append("\n").append(line)
                                }
                            }
                        }
                    }
                }
                
                emitCurrentError(currentError, currentTag, currentSeverity)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        monitoringJob?.cancel()
        logcatProcess?.destroy()
    }
}
