package com.lamontlabs.quantravision.memory

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import timber.log.Timber

/**
 * Memory monitor for tracking usage and triggering cleanup.
 * Integrates with Android's memory management system.
 */
class MemoryMonitor(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val runtime = Runtime.getRuntime()
    
    data class MemoryUsage(
        val usedMB: Double,
        val totalMB: Double,
        val maxMB: Double,
        val availableMB: Double,
        val usagePercent: Double,
        val isLow: Boolean,
        val isCritical: Boolean
    )
    
    enum class MemoryLevel {
        NORMAL, MODERATE, HIGH, CRITICAL
    }
    
    private val cleanupCallbacks = mutableListOf<() -> Unit>()
    
    /**
     * Get current memory usage.
     */
    fun getUsage(): MemoryUsage {
        val used = runtime.totalMemory() - runtime.freeMemory()
        val total = runtime.totalMemory()
        val max = runtime.maxMemory()
        val available = max - used
        
        val usedMB = used / (1024.0 * 1024.0)
        val totalMB = total / (1024.0 * 1024.0)
        val maxMB = max / (1024.0 * 1024.0)
        val availableMB = available / (1024.0 * 1024.0)
        
        val usagePercent = (used.toDouble() / max) * 100
        
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        return MemoryUsage(
            usedMB = usedMB,
            totalMB = totalMB,
            maxMB = maxMB,
            availableMB = availableMB,
            usagePercent = usagePercent,
            isLow = memInfo.lowMemory,
            isCritical = usagePercent > 90
        )
    }
    
    /**
     * Determine current memory level.
     */
    fun getMemoryLevel(): MemoryLevel {
        val usage = getUsage()
        
        return when {
            usage.isCritical || usage.usagePercent > 90 -> MemoryLevel.CRITICAL
            usage.usagePercent > 80 -> MemoryLevel.HIGH
            usage.usagePercent > 60 -> MemoryLevel.MODERATE
            else -> MemoryLevel.NORMAL
        }
    }
    
    /**
     * Register cleanup callback to be called on memory pressure.
     */
    fun registerCleanupCallback(callback: () -> Unit) {
        cleanupCallbacks.add(callback)
    }
    
    /**
     * Trigger garbage collection.
     * Use sparingly - let system manage GC normally.
     */
    fun triggerGC() {
        Timber.i("Triggering garbage collection")
        System.gc()
        
        // Log memory before/after
        val usageAfter = getUsage()
        Timber.i("Memory after GC: ${String.format("%.2f", usageAfter.usedMB)}MB / ${String.format("%.2f", usageAfter.maxMB)}MB (${String.format("%.1f", usageAfter.usagePercent)}%)")
    }
    
    /**
     * Release resources on memory pressure.
     * Triggers cleanup callbacks and pool trimming.
     */
    fun releaseResources(level: MemoryLevel = MemoryLevel.HIGH) {
        Timber.w("Releasing resources (level: $level)")
        
        val usageBefore = getUsage()
        
        when (level) {
            MemoryLevel.CRITICAL -> {
                // Aggressive cleanup
                MatPool.clear()
                BitmapPool.clear()
                cleanupCallbacks.forEach { it() }
                triggerGC()
            }
            MemoryLevel.HIGH -> {
                // Moderate cleanup
                MatPool.trim()
                BitmapPool.trim()
                cleanupCallbacks.forEach { it() }
            }
            MemoryLevel.MODERATE -> {
                // Light cleanup
                MatPool.trim()
                BitmapPool.trim()
            }
            MemoryLevel.NORMAL -> {
                // No cleanup needed
            }
        }
        
        val usageAfter = getUsage()
        val freedMB = usageBefore.usedMB - usageAfter.usedMB
        
        Timber.i("Released ${String.format("%.2f", freedMB)}MB of memory")
    }
    
    /**
     * Check if cleanup is needed and trigger if necessary.
     */
    fun checkAndCleanup() {
        val level = getMemoryLevel()
        
        if (level == MemoryLevel.CRITICAL || level == MemoryLevel.HIGH) {
            releaseResources(level)
        }
    }
    
    /**
     * Handle ComponentCallbacks2 memory trim event.
     */
    fun onTrimMemory(level: Int) {
        Timber.i("onTrimMemory: $level")
        
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                releaseResources(MemoryLevel.CRITICAL)
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                releaseResources(MemoryLevel.HIGH)
            }
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                releaseResources(MemoryLevel.MODERATE)
            }
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // App in background, can be more aggressive
                releaseResources(MemoryLevel.HIGH)
            }
        }
    }
    
    /**
     * Log memory statistics.
     */
    fun logStats() {
        val usage = getUsage()
        val level = getMemoryLevel()
        
        Timber.i(
            "Memory Stats:\n" +
            "  Used: ${String.format("%.2f", usage.usedMB)}MB\n" +
            "  Total: ${String.format("%.2f", usage.totalMB)}MB\n" +
            "  Max: ${String.format("%.2f", usage.maxMB)}MB\n" +
            "  Available: ${String.format("%.2f", usage.availableMB)}MB\n" +
            "  Usage: ${String.format("%.1f", usage.usagePercent)}%\n" +
            "  Level: $level\n" +
            "  Low Memory: ${usage.isLow}"
        )
        
        // Log pool stats
        MatPool.logStats()
        BitmapPool.logStats()
    }
    
    /**
     * Start periodic memory monitoring.
     */
    fun startMonitoring(intervalMs: Long = 30000L) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        val runnable = object : Runnable {
            override fun run() {
                checkAndCleanup()
                handler.postDelayed(this, intervalMs)
            }
        }
        
        handler.post(runnable)
        Timber.d("Started memory monitoring (interval: ${intervalMs}ms)")
    }
}
