package com.lamontlabs.quantravision.performance

import android.os.SystemClock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Performance profiler for detection pipeline monitoring.
 * Tracks detection time, FPS, CPU usage, and memory metrics.
 */
object PerformanceProfiler {
    
    private const val SLOW_OPERATION_THRESHOLD_MS = 50L
    private const val FPS_WINDOW_SIZE = 30
    
    // Metrics tracking
    private val detectionTimes = mutableListOf<Long>()
    private val fpsWindow = ArrayDeque<Long>(FPS_WINDOW_SIZE)
    private val operationCounts = ConcurrentHashMap<String, AtomicLong>()
    private val operationDurations = ConcurrentHashMap<String, AtomicLong>()
    private val slowOperations = mutableListOf<SlowOperation>()
    
    private var lastFrameTime = 0L
    private var totalDetections = 0L
    private var totalDetectionTime = 0L
    
    data class SlowOperation(
        val operationName: String,
        val durationMs: Long,
        val timestamp: Long
    )
    
    data class PerformanceMetrics(
        val averageDetectionTimeMs: Double,
        val currentFps: Double,
        val totalDetections: Long,
        val memoryUsageMB: Double,
        val slowOperationCount: Int,
        val operationBreakdown: Map<String, OperationStats>
    )
    
    data class OperationStats(
        val count: Long,
        val totalDurationMs: Long,
        val averageDurationMs: Double
    )
    
    /**
     * Start timing an operation.
     */
    fun startOperation(operationName: String): Long {
        return SystemClock.elapsedRealtime()
    }
    
    /**
     * End timing an operation and record metrics.
     */
    fun endOperation(operationName: String, startTime: Long) {
        val duration = SystemClock.elapsedRealtime() - startTime
        
        // Record operation
        operationCounts.getOrPut(operationName) { AtomicLong(0) }.incrementAndGet()
        operationDurations.getOrPut(operationName) { AtomicLong(0) }.addAndGet(duration)
        
        // Log slow operations
        if (duration > SLOW_OPERATION_THRESHOLD_MS) {
            synchronized(slowOperations) {
                slowOperations.add(SlowOperation(operationName, duration, System.currentTimeMillis()))
                
                // Keep only last 100 slow operations
                if (slowOperations.size > 100) {
                    slowOperations.removeAt(0)
                }
            }
            
            Timber.w("Slow operation: $operationName took ${duration}ms")
        }
    }
    
    /**
     * Record a detection completion.
     */
    fun recordDetection(durationMs: Long) {
        synchronized(detectionTimes) {
            detectionTimes.add(durationMs)
            
            // Keep only last 100 detections
            if (detectionTimes.size > 100) {
                detectionTimes.removeAt(0)
            }
        }
        
        totalDetections++
        totalDetectionTime += durationMs
        
        // Update FPS
        val now = SystemClock.elapsedRealtime()
        if (lastFrameTime > 0) {
            synchronized(fpsWindow) {
                fpsWindow.addLast(now - lastFrameTime)
                if (fpsWindow.size > FPS_WINDOW_SIZE) {
                    fpsWindow.removeFirst()
                }
            }
        }
        lastFrameTime = now
    }
    
    /**
     * Get current performance metrics.
     */
    fun getMetrics(): PerformanceMetrics {
        val avgDetectionTime = synchronized(detectionTimes) {
            if (detectionTimes.isNotEmpty()) {
                detectionTimes.average()
            } else {
                0.0
            }
        }
        
        val fps = synchronized(fpsWindow) {
            if (fpsWindow.isNotEmpty()) {
                val avgFrameTimeMs = fpsWindow.average()
                if (avgFrameTimeMs > 0) {
                    1000.0 / avgFrameTimeMs
                } else {
                    0.0
                }
            } else {
                0.0
            }
        }
        
        val memoryUsage = getMemoryUsageMB()
        
        val opBreakdown = operationCounts.mapNotNull { (name, count) ->
            val totalDuration = operationDurations[name]?.get() ?: 0L
            val countValue = count.get()
            
            if (countValue > 0) {
                name to OperationStats(
                    count = countValue,
                    totalDurationMs = totalDuration,
                    averageDurationMs = totalDuration.toDouble() / countValue
                )
            } else {
                null
            }
        }.toMap()
        
        return PerformanceMetrics(
            averageDetectionTimeMs = avgDetectionTime,
            currentFps = fps,
            totalDetections = totalDetections,
            memoryUsageMB = memoryUsage,
            slowOperationCount = slowOperations.size,
            operationBreakdown = opBreakdown
        )
    }
    
    /**
     * Get memory usage in MB.
     */
    private fun getMemoryUsageMB(): Double {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        return usedMemory / (1024.0 * 1024.0)
    }
    
    /**
     * Get slow operations list.
     */
    fun getSlowOperations(): List<SlowOperation> {
        return synchronized(slowOperations) {
            slowOperations.toList()
        }
    }
    
    /**
     * Reset all metrics.
     */
    fun reset() {
        synchronized(detectionTimes) {
            detectionTimes.clear()
        }
        synchronized(fpsWindow) {
            fpsWindow.clear()
        }
        operationCounts.clear()
        operationDurations.clear()
        synchronized(slowOperations) {
            slowOperations.clear()
        }
        totalDetections = 0L
        totalDetectionTime = 0L
        lastFrameTime = 0L
    }
    
    /**
     * Log performance summary.
     */
    fun logSummary() {
        val metrics = getMetrics()
        
        Timber.i(
            "Performance Summary:\n" +
            "  Avg Detection Time: ${String.format("%.2f", metrics.averageDetectionTimeMs)}ms\n" +
            "  Current FPS: ${String.format("%.1f", metrics.currentFps)}\n" +
            "  Total Detections: ${metrics.totalDetections}\n" +
            "  Memory Usage: ${String.format("%.2f", metrics.memoryUsageMB)}MB\n" +
            "  Slow Operations: ${metrics.slowOperationCount}"
        )
        
        // Log operation breakdown
        metrics.operationBreakdown.entries
            .sortedByDescending { it.value.totalDurationMs }
            .take(5)
            .forEach { (name, stats) ->
                Timber.i(
                    "  $name: ${stats.count} calls, " +
                    "${String.format("%.2f", stats.averageDurationMs)}ms avg, " +
                    "${stats.totalDurationMs}ms total"
                )
            }
    }
}
