package com.lamontlabs.quantravision.memory

import org.opencv.core.CvType
import org.opencv.core.Mat
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Object pool for OpenCV Mat reuse.
 * Reduces allocation overhead and memory fragmentation.
 */
object MatPool {
    
    private val pools = ConcurrentHashMap<MatSpec, ConcurrentLinkedQueue<Mat>>()
    private const val MAX_POOL_SIZE = 50
    private const val PRE_ALLOCATE_SIZE = 5
    
    data class MatSpec(
        val rows: Int,
        val cols: Int,
        val type: Int
    ) {
        companion object {
            fun from(mat: Mat): MatSpec {
                return MatSpec(mat.rows(), mat.cols(), mat.type())
            }
        }
    }
    
    data class PoolStats(
        val totalPools: Int,
        val totalMats: Int,
        val acquisitions: Long,
        val releases: Long,
        val memoryUsageMB: Double
    )
    
    private var acquisitionCount = 0L
    private var releaseCount = 0L
    
    init {
        // Pre-allocate common sizes
        preAllocateCommonSizes()
    }
    
    /**
     * Pre-allocate common Mat sizes to reduce allocation overhead.
     */
    private fun preAllocateCommonSizes() {
        val commonSizes = listOf(
            MatSpec(480, 640, CvType.CV_8UC1),   // VGA grayscale
            MatSpec(720, 1280, CvType.CV_8UC1),  // 720p grayscale
            MatSpec(1080, 1920, CvType.CV_8UC1), // 1080p grayscale
            MatSpec(480, 640, CvType.CV_8UC4),   // VGA RGBA
            MatSpec(100, 100, CvType.CV_8UC1),   // Small template
            MatSpec(200, 200, CvType.CV_8UC1)    // Medium template
        )
        
        commonSizes.forEach { spec ->
            val pool = pools.getOrPut(spec) { ConcurrentLinkedQueue() }
            repeat(PRE_ALLOCATE_SIZE) {
                pool.offer(Mat(spec.rows, spec.cols, spec.type))
            }
        }
        
        Timber.d("MatPool: Pre-allocated ${commonSizes.size * PRE_ALLOCATE_SIZE} Mats")
    }
    
    /**
     * Acquire a Mat from the pool or create new if none available.
     * 
     * @param rows Number of rows
     * @param cols Number of columns
     * @param type Mat type (e.g., CvType.CV_8UC1)
     * @return Mat instance
     */
    fun acquire(rows: Int, cols: Int, type: Int): Mat {
        acquisitionCount++
        
        val spec = MatSpec(rows, cols, type)
        val pool = pools.getOrPut(spec) { ConcurrentLinkedQueue() }
        
        val mat = pool.poll()
        
        return if (mat != null) {
            // Reuse from pool
            mat
        } else {
            // Create new
            Mat(rows, cols, type)
        }
    }
    
    /**
     * Release a Mat back to the pool for reuse.
     * Mat will be cleared before returning to pool.
     * 
     * @param mat Mat to release
     */
    fun release(mat: Mat?) {
        if (mat == null || mat.empty()) {
            return
        }
        
        releaseCount++
        
        try {
            val spec = MatSpec.from(mat)
            val pool = pools.getOrPut(spec) { ConcurrentLinkedQueue() }
            
            // Only keep if pool not full
            if (pool.size < MAX_POOL_SIZE) {
                // Clear mat data (zero out)
                mat.setTo(org.opencv.core.Scalar(0.0))
                pool.offer(mat)
            } else {
                // Pool full, release the Mat
                mat.release()
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to release Mat to pool")
            mat.release()
        }
    }
    
    /**
     * Clear all pools and release all Mats.
     * Use on memory pressure or app shutdown.
     */
    fun clear() {
        var totalReleased = 0
        
        pools.values.forEach { pool ->
            while (true) {
                val mat = pool.poll() ?: break
                mat.release()
                totalReleased++
            }
        }
        
        pools.clear()
        
        Timber.i("MatPool: Cleared $totalReleased Mats")
        
        // Re-allocate common sizes
        preAllocateCommonSizes()
    }
    
    /**
     * Trim pools to reduce memory usage.
     * Removes excess Mats beyond minimum size.
     */
    fun trim() {
        var totalTrimmed = 0
        
        pools.values.forEach { pool ->
            val excess = pool.size - PRE_ALLOCATE_SIZE
            if (excess > 0) {
                repeat(excess) {
                    val mat = pool.poll()
                    mat?.release()
                    totalTrimmed++
                }
            }
        }
        
        if (totalTrimmed > 0) {
            Timber.d("MatPool: Trimmed $totalTrimmed excess Mats")
        }
    }
    
    /**
     * Get pool statistics.
     */
    fun getStats(): PoolStats {
        val totalMats = pools.values.sumOf { it.size }
        
        // Estimate memory usage
        var memoryBytes = 0L
        pools.forEach { (spec, pool) ->
            val bytesPerMat = spec.rows * spec.cols * CvType.channels(spec.type) * 
                             when {
                                 spec.type == CvType.CV_8UC1 || spec.type == CvType.CV_8UC4 -> 1
                                 spec.type == CvType.CV_32FC1 -> 4
                                 else -> 1
                             }
            memoryBytes += pool.size * bytesPerMat
        }
        
        val memoryMB = memoryBytes / (1024.0 * 1024.0)
        
        return PoolStats(
            totalPools = pools.size,
            totalMats = totalMats,
            acquisitions = acquisitionCount,
            releases = releaseCount,
            memoryUsageMB = memoryMB
        )
    }
    
    /**
     * Log pool statistics.
     */
    fun logStats() {
        val stats = getStats()
        Timber.i(
            "MatPool Stats:\n" +
            "  Pools: ${stats.totalPools}\n" +
            "  Total Mats: ${stats.totalMats}\n" +
            "  Acquisitions: ${stats.acquisitions}\n" +
            "  Releases: ${stats.releases}\n" +
            "  Memory: ${String.format("%.2f", stats.memoryUsageMB)} MB"
        )
    }
    
    /**
     * Handle memory pressure event.
     * Aggressively trim pools.
     */
    fun onMemoryPressure() {
        Timber.w("MatPool: Memory pressure detected, clearing pools")
        clear()
    }
}
