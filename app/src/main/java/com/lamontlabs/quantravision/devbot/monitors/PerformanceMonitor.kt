package com.lamontlabs.quantravision.devbot.monitors

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import com.lamontlabs.quantravision.devbot.data.PerformanceMetric
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicLong

data class PerformanceIssue(
    val description: String,
    val type: PerformanceMetric,
    val value: Long,
    val threshold: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class PerformanceThresholds(
    val memoryThresholdMB: Long = 300L,
    val memoryPercentageThreshold: Int = 85,
    val frameDropThreshold: Int = 3,
    val frameTimeThresholdMs: Long = 16L,
    val jankThresholdMs: Long = 100L
)

class PerformanceMonitor(
    private val context: Context,
    private val thresholds: PerformanceThresholds = PerformanceThresholds()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null
    private var frameCallbackActive = false
    
    private val _issues = MutableSharedFlow<PerformanceIssue>(
        replay = 20,
        extraBufferCapacity = 100
    )
    val issues: SharedFlow<PerformanceIssue> = _issues.asSharedFlow()
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val runtime = Runtime.getRuntime()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private var lastFrameTimeNanos = AtomicLong(0L)
    private var droppedFrameCount = 0
    private var consecutiveJankyFrames = 0
    
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!frameCallbackActive) return
            
            val lastTime = lastFrameTimeNanos.get()
            if (lastTime != 0L) {
                val frameTimeMs = (frameTimeNanos - lastTime) / 1_000_000
                
                if (frameTimeMs > thresholds.frameTimeThresholdMs) {
                    val droppedFrames = (frameTimeMs / thresholds.frameTimeThresholdMs).toInt() - 1
                    if (droppedFrames > 0) {
                        droppedFrameCount += droppedFrames
                        
                        if (droppedFrames >= thresholds.frameDropThreshold) {
                            scope.launch {
                                _issues.emit(
                                    PerformanceIssue(
                                        description = "Frame drop detected: $droppedFrames frames dropped (${frameTimeMs}ms frame time)",
                                        type = PerformanceMetric.FRAME_DROP,
                                        value = droppedFrames.toLong(),
                                        threshold = thresholds.frameDropThreshold.toLong()
                                    )
                                )
                            }
                        }
                    }
                }
                
                if (frameTimeMs > thresholds.jankThresholdMs) {
                    consecutiveJankyFrames++
                    if (consecutiveJankyFrames >= 2) {
                        scope.launch {
                            _issues.emit(
                                PerformanceIssue(
                                    description = "UI jank detected: ${frameTimeMs}ms frame time (threshold: ${thresholds.jankThresholdMs}ms)",
                                    type = PerformanceMetric.UI_THREAD_BLOCK,
                                    value = frameTimeMs,
                                    threshold = thresholds.jankThresholdMs
                                )
                            )
                        }
                    }
                } else {
                    consecutiveJankyFrames = 0
                }
            }
            
            lastFrameTimeNanos.set(frameTimeNanos)
            
            if (frameCallbackActive) {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }
    
    fun startMonitoring() {
        if (monitoringJob?.isActive == true) return
        
        monitoringJob = scope.launch {
            while (isActive) {
                checkMemoryUsage()
                delay(1000)
            }
        }
        
        startFrameMonitoring()
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        stopFrameMonitoring()
    }
    
    private fun startFrameMonitoring() {
        if (frameCallbackActive) return
        
        frameCallbackActive = true
        lastFrameTimeNanos.set(0L)
        droppedFrameCount = 0
        consecutiveJankyFrames = 0
        
        mainHandler.post {
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }
    
    private fun stopFrameMonitoring() {
        frameCallbackActive = false
        mainHandler.post {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
        }
    }
    
    private suspend fun checkMemoryUsage() {
        val memInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memInfo)
        
        val totalMemoryMB = memInfo.totalPss / 1024L
        
        if (totalMemoryMB > thresholds.memoryThresholdMB) {
            _issues.emit(
                PerformanceIssue(
                    description = "High memory usage detected: ${totalMemoryMB}MB (threshold: ${thresholds.memoryThresholdMB}MB)",
                    type = PerformanceMetric.MEMORY_USAGE,
                    value = totalMemoryMB,
                    threshold = thresholds.memoryThresholdMB
                )
            )
        }
        
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val memoryPercentage = (usedMemory * 100 / maxMemory)
        
        if (memoryPercentage > thresholds.memoryPercentageThreshold) {
            _issues.emit(
                PerformanceIssue(
                    description = "Memory pressure: ${memoryPercentage}% used (${usedMemory}MB / ${maxMemory}MB)",
                    type = PerformanceMetric.MEMORY_USAGE,
                    value = memoryPercentage,
                    threshold = thresholds.memoryPercentageThreshold.toLong()
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
