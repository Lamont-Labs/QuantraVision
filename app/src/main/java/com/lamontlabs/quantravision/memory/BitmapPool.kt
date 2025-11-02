package com.lamontlabs.quantravision.memory

import android.graphics.Bitmap
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Object pool for Bitmap reuse.
 * Reduces allocation overhead and garbage collection pressure.
 */
object BitmapPool {
    
    private val pools = ConcurrentHashMap<BitmapSpec, ConcurrentLinkedQueue<Bitmap>>()
    private const val MAX_POOL_SIZE = 20
    private const val PRE_ALLOCATE_SIZE = 3
    
    data class BitmapSpec(
        val width: Int,
        val height: Int,
        val config: Bitmap.Config
    ) {
        companion object {
            fun from(bitmap: Bitmap): BitmapSpec {
                return BitmapSpec(bitmap.width, bitmap.height, bitmap.config)
            }
        }
    }
    
    data class PoolStats(
        val totalPools: Int,
        val totalBitmaps: Int,
        val acquisitions: Long,
        val releases: Long,
        val memoryUsageMB: Double
    )
    
    private var acquisitionCount = 0L
    private var releaseCount = 0L
    
    init {
        preAllocateCommonSizes()
    }
    
    /**
     * Pre-allocate common bitmap sizes.
     */
    private fun preAllocateCommonSizes() {
        val commonSizes = listOf(
            BitmapSpec(640, 480, Bitmap.Config.ARGB_8888),   // VGA
            BitmapSpec(1280, 720, Bitmap.Config.ARGB_8888),  // 720p
            BitmapSpec(100, 100, Bitmap.Config.ARGB_8888),   // Thumbnail
            BitmapSpec(200, 200, Bitmap.Config.ARGB_8888)    // Small preview
        )
        
        commonSizes.forEach { spec ->
            val pool = pools.getOrPut(spec) { ConcurrentLinkedQueue() }
            repeat(PRE_ALLOCATE_SIZE) {
                try {
                    pool.offer(Bitmap.createBitmap(spec.width, spec.height, spec.config))
                } catch (e: OutOfMemoryError) {
                    Timber.e("Failed to pre-allocate bitmap: ${spec.width}x${spec.height}")
                }
            }
        }
        
        Timber.d("BitmapPool: Pre-allocated ${commonSizes.size * PRE_ALLOCATE_SIZE} bitmaps")
    }
    
    /**
     * Acquire a Bitmap from the pool.
     * 
     * @param width Bitmap width
     * @param height Bitmap height
     * @param config Bitmap configuration
     * @return Bitmap instance or null if allocation fails
     */
    fun acquire(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
        acquisitionCount++
        
        val spec = BitmapSpec(width, height, config)
        val pool = pools.getOrPut(spec) { ConcurrentLinkedQueue() }
        
        val bitmap = pool.poll()
        
        return if (bitmap != null && !bitmap.isRecycled) {
            // Reuse from pool - clear previous content
            bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
            bitmap
        } else {
            // Create new
            try {
                Bitmap.createBitmap(width, height, config)
            } catch (e: OutOfMemoryError) {
                Timber.e("Failed to allocate bitmap: ${width}x${height}")
                null
            }
        }
    }
    
    /**
     * Release a Bitmap back to the pool.
     * 
     * @param bitmap Bitmap to release
     */
    fun release(bitmap: Bitmap?) {
        if (bitmap == null || bitmap.isRecycled) {
            return
        }
        
        releaseCount++
        
        try {
            val spec = BitmapSpec.from(bitmap)
            val pool = pools.getOrPut(spec) { ConcurrentLinkedQueue() }
            
            // Only keep if pool not full
            if (pool.size < MAX_POOL_SIZE) {
                pool.offer(bitmap)
            } else {
                // Pool full, recycle the bitmap
                bitmap.recycle()
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to release Bitmap to pool")
            bitmap.recycle()
        }
    }
    
    /**
     * Clear all pools and recycle all bitmaps.
     */
    fun clear() {
        var totalRecycled = 0
        
        pools.values.forEach { pool ->
            while (true) {
                val bitmap = pool.poll() ?: break
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                    totalRecycled++
                }
            }
        }
        
        pools.clear()
        
        Timber.i("BitmapPool: Cleared $totalRecycled bitmaps")
        
        // Re-allocate common sizes
        preAllocateCommonSizes()
    }
    
    /**
     * Trim pools to reduce memory usage.
     */
    fun trim() {
        var totalTrimmed = 0
        
        pools.values.forEach { pool ->
            val excess = pool.size - PRE_ALLOCATE_SIZE
            if (excess > 0) {
                repeat(excess) {
                    val bitmap = pool.poll()
                    if (bitmap != null && !bitmap.isRecycled) {
                        bitmap.recycle()
                        totalTrimmed++
                    }
                }
            }
        }
        
        if (totalTrimmed > 0) {
            Timber.d("BitmapPool: Trimmed $totalTrimmed excess bitmaps")
        }
    }
    
    /**
     * Get pool statistics.
     */
    fun getStats(): PoolStats {
        val totalBitmaps = pools.values.sumOf { it.size }
        
        // Estimate memory usage
        var memoryBytes = 0L
        pools.forEach { (spec, pool) ->
            val bytesPerBitmap = spec.width * spec.height * when (spec.config) {
                Bitmap.Config.ARGB_8888 -> 4
                Bitmap.Config.RGB_565 -> 2
                Bitmap.Config.ALPHA_8 -> 1
                else -> 4
            }
            memoryBytes += pool.size * bytesPerBitmap
        }
        
        val memoryMB = memoryBytes / (1024.0 * 1024.0)
        
        return PoolStats(
            totalPools = pools.size,
            totalBitmaps = totalBitmaps,
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
            "BitmapPool Stats:\n" +
            "  Pools: ${stats.totalPools}\n" +
            "  Total Bitmaps: ${stats.totalBitmaps}\n" +
            "  Acquisitions: ${stats.acquisitions}\n" +
            "  Releases: ${stats.releases}\n" +
            "  Memory: ${String.format("%.2f", stats.memoryUsageMB)} MB"
        )
    }
    
    /**
     * Handle memory pressure event.
     */
    fun onMemoryPressure() {
        Timber.w("BitmapPool: Memory pressure detected, clearing pools")
        clear()
    }
    
    /**
     * Create a downsampled bitmap for thumbnails.
     * Uses pool for efficiency.
     */
    fun createThumbnail(source: Bitmap, maxSize: Int): Bitmap? {
        val scale = minOf(
            maxSize.toFloat() / source.width,
            maxSize.toFloat() / source.height
        )
        
        val targetWidth = (source.width * scale).toInt()
        val targetHeight = (source.height * scale).toInt()
        
        val thumbnail = acquire(targetWidth, targetHeight, Bitmap.Config.RGB_565)
        
        return thumbnail?.let {
            val canvas = android.graphics.Canvas(it)
            val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
            
            val srcRect = android.graphics.Rect(0, 0, source.width, source.height)
            val dstRect = android.graphics.Rect(0, 0, targetWidth, targetHeight)
            
            canvas.drawBitmap(source, srcRect, dstRect, paint)
            it
        }
    }
}
