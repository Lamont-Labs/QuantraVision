package com.lamontlabs.quantravision.cache

import android.graphics.Bitmap
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * LRU cache for chart perceptual hashes.
 * Detects chart changes using histogram comparison.
 */
class ChartHashCache(
    private val maxSize: Int = 100,
    private val changeThreshold: Double = 0.10  // 10% histogram change
) {
    
    private val cache = object : LinkedHashMap<String, CacheEntry>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean {
            return size > maxSize
        }
    }
    
    private val accessOrder = ConcurrentHashMap<String, Long>()
    
    data class CacheEntry(
        val hash: Long,
        val regionalHashes: List<Long>,
        val histogram: HistogramData,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    data class HistogramData(
        val values: FloatArray,
        val checksum: Long
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HistogramData) return false
            return checksum == other.checksum
        }
        
        override fun hashCode(): Int {
            return checksum.hashCode()
        }
    }
    
    data class CacheStats(
        val size: Int,
        val hits: Long,
        val misses: Long,
        val invalidations: Long,
        val hitRate: Double
    )
    
    private var cacheHits = 0L
    private var cacheMisses = 0L
    private var cacheInvalidations = 0L
    
    /**
     * Check if chart has changed since last cached version.
     * Returns null if not in cache or changed significantly.
     */
    @Synchronized
    fun checkForChanges(chartId: String, currentBitmap: Bitmap): Boolean {
        val entry = cache[chartId]
        
        if (entry == null) {
            cacheMisses++
            return true  // Not cached, treat as changed
        }
        
        // Compute current histogram
        val currentHistogram = computeHistogram(currentBitmap)
        
        // Compare histograms
        val similarity = compareHistograms(entry.histogram, currentHistogram)
        val changePercent = 1.0 - similarity
        
        if (changePercent > changeThreshold) {
            // Significant change detected
            cacheInvalidations++
            Timber.d("Chart $chartId changed by ${String.format("%.1f", changePercent * 100)}%")
            return true
        }
        
        // No significant change
        cacheHits++
        accessOrder[chartId] = System.currentTimeMillis()
        return false
    }
    
    /**
     * Store chart hash in cache.
     */
    @Synchronized
    fun put(chartId: String, bitmap: Bitmap) {
        try {
            val hash = PerceptualHasher.computeHash(bitmap)
            val regionalHashes = PerceptualHasher.computeRegionalHashes(bitmap)
            val histogram = computeHistogram(bitmap)
            
            val entry = CacheEntry(
                hash = hash,
                regionalHashes = regionalHashes,
                histogram = histogram
            )
            
            cache[chartId] = entry
            accessOrder[chartId] = System.currentTimeMillis()
            
            Timber.d("Cached chart $chartId (cache size: ${cache.size})")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to cache chart $chartId")
        }
    }
    
    /**
     * Get cached hash for a chart.
     */
    @Synchronized
    fun get(chartId: String): Long? {
        val entry = cache[chartId]
        if (entry != null) {
            accessOrder[chartId] = System.currentTimeMillis()
            return entry.hash
        }
        return null
    }
    
    /**
     * Invalidate cache entry.
     */
    @Synchronized
    fun invalidate(chartId: String) {
        cache.remove(chartId)
        accessOrder.remove(chartId)
        cacheInvalidations++
    }
    
    /**
     * Clear entire cache.
     */
    @Synchronized
    fun clear() {
        cache.clear()
        accessOrder.clear()
        Timber.d("Chart hash cache cleared")
    }
    
    /**
     * Get cache statistics.
     */
    fun getStats(): CacheStats {
        val total = cacheHits + cacheMisses
        val hitRate = if (total > 0) {
            cacheHits.toDouble() / total
        } else {
            0.0
        }
        
        return CacheStats(
            size = cache.size,
            hits = cacheHits,
            misses = cacheMisses,
            invalidations = cacheInvalidations,
            hitRate = hitRate
        )
    }
    
    /**
     * Compute simplified histogram for comparison.
     */
    private fun computeHistogram(bitmap: Bitmap): HistogramData {
        val histogram = FloatArray(64)  // 64 bins for speed
        val pixels = IntArray(bitmap.width * bitmap.height)
        
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        // Compute grayscale histogram
        for (pixel in pixels) {
            val gray = (android.graphics.Color.red(pixel) * 0.299f +
                        android.graphics.Color.green(pixel) * 0.587f +
                        android.graphics.Color.blue(pixel) * 0.114f).toInt()
            
            val bin = (gray * 64 / 256).coerceIn(0, 63)
            histogram[bin]++
        }
        
        // Normalize
        val sum = histogram.sum()
        if (sum > 0) {
            for (i in histogram.indices) {
                histogram[i] /= sum
            }
        }
        
        // Compute checksum
        var checksum = 0L
        for (value in histogram) {
            checksum = checksum * 31 + (value * 1000).toLong()
        }
        
        return HistogramData(histogram, checksum)
    }
    
    /**
     * Compare two histograms using correlation.
     */
    private fun compareHistograms(hist1: HistogramData, hist2: HistogramData): Double {
        val h1 = hist1.values
        val h2 = hist2.values
        
        // Compute correlation
        var sum1 = 0.0
        var sum2 = 0.0
        var sum1Sq = 0.0
        var sum2Sq = 0.0
        var pSum = 0.0
        val n = h1.size
        
        for (i in 0 until n) {
            sum1 += h1[i]
            sum2 += h2[i]
            sum1Sq += h1[i] * h1[i]
            sum2Sq += h2[i] * h2[i]
            pSum += h1[i] * h2[i]
        }
        
        val num = pSum - (sum1 * sum2 / n)
        val den = Math.sqrt((sum1Sq - sum1 * sum1 / n) * (sum2Sq - sum2 * sum2 / n))
        
        return if (den == 0.0) 0.0 else num / den
    }
    
    /**
     * Log cache statistics.
     */
    fun logStats() {
        val stats = getStats()
        Timber.i(
            "ChartHashCache Stats:\n" +
            "  Size: ${stats.size}/${maxSize}\n" +
            "  Hit Rate: ${String.format("%.1f", stats.hitRate * 100)}%\n" +
            "  Hits: ${stats.hits}, Misses: ${stats.misses}\n" +
            "  Invalidations: ${stats.invalidations}"
        )
    }
}
