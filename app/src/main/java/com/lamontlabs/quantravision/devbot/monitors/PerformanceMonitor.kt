package com.lamontlabs.quantravision.devbot.monitors

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Looper
import com.lamontlabs.quantravision.devbot.data.PerformanceMetric
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class PerformanceIssue(
    val description: String,
    val type: PerformanceMetric,
    val value: Long,
    val threshold: Long,
    val timestamp: Long = System.currentTimeMillis()
)

class PerformanceMonitor(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null
    
    private val _issues = MutableSharedFlow<PerformanceIssue>(
        replay = 20,
        extraBufferCapacity = 100
    )
    val issues: SharedFlow<PerformanceIssue> = _issues.asSharedFlow()
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val runtime = Runtime.getRuntime()
    
    private val MEMORY_THRESHOLD_MB = 300L
    private val UI_BLOCK_THRESHOLD_MS = 100L
    private val GC_PAUSE_THRESHOLD_MS = 50L
    
    private var lastGcTime = 0L
    private var lastMemoryCheck = 0L
    
    fun startMonitoring() {
        if (monitoringJob?.isActive == true) return
        
        monitoringJob = scope.launch {
            while (isActive) {
                checkMemoryUsage()
                checkUIThread()
                delay(1000)
            }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
    }
    
    private suspend fun checkMemoryUsage() {
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        
        val totalMemoryMB = memInfo.totalPss / 1024L
        
        if (totalMemoryMB > MEMORY_THRESHOLD_MB) {
            _issues.emit(
                PerformanceIssue(
                    description = "High memory usage detected: ${totalMemoryMB}MB (threshold: ${MEMORY_THRESHOLD_MB}MB)",
                    type = PerformanceMetric.MEMORY_USAGE,
                    value = totalMemoryMB,
                    threshold = MEMORY_THRESHOLD_MB
                )
            )
        }
        
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val memoryPercentage = (usedMemory * 100 / maxMemory)
        
        if (memoryPercentage > 85) {
            _issues.emit(
                PerformanceIssue(
                    description = "Memory pressure: ${memoryPercentage}% used (${usedMemory}MB / ${maxMemory}MB)",
                    type = PerformanceMetric.MEMORY_USAGE,
                    value = memoryPercentage,
                    threshold = 85
                )
            )
        }
    }
    
    private suspend fun checkUIThread() {
        val mainLooper = Looper.getMainLooper()
        
        if (mainLooper.thread.state == Thread.State.BLOCKED ||
            mainLooper.thread.state == Thread.State.WAITING) {
            
            _issues.emit(
                PerformanceIssue(
                    description = "Main thread blocked - potential ANR risk",
                    type = PerformanceMetric.UI_THREAD_BLOCK,
                    value = 1,
                    threshold = 0
                )
            )
        }
    }
    
    fun reportSlowOperation(operationName: String, durationMs: Long, thresholdMs: Long = 100) {
        if (durationMs > thresholdMs) {
            scope.launch {
                _issues.emit(
                    PerformanceIssue(
                        description = "$operationName took ${durationMs}ms (threshold: ${thresholdMs}ms)",
                        type = PerformanceMetric.UI_THREAD_BLOCK,
                        value = durationMs,
                        threshold = thresholdMs
                    )
                )
            }
        }
    }
    
    fun reportFrameDrop(droppedFrames: Int) {
        scope.launch {
            _issues.emit(
                PerformanceIssue(
                    description = "Frame drop detected: $droppedFrames frames",
                    type = PerformanceMetric.FRAME_DROP,
                    value = droppedFrames.toLong(),
                    threshold = 5
                )
            )
        }
    }
    
    fun getCurrentMemoryUsage(): MemoryStats {
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val totalMemory = runtime.totalMemory() / (1024 * 1024)
        val freeMemory = runtime.freeMemory() / (1024 * 1024)
        val usedMemory = totalMemory - freeMemory
        
        return MemoryStats(
            maxMB = maxMemory,
            totalMB = totalMemory,
            usedMB = usedMemory,
            freeMB = freeMemory,
            pssMB = memInfo.totalPss / 1024L
        )
    }
}

data class MemoryStats(
    val maxMB: Long,
    val totalMB: Long,
    val usedMB: Long,
    val freeMB: Long,
    val pssMB: Long
)
