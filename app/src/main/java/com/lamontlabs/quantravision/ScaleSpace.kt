package com.lamontlabs.quantravision

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Enhanced multi-scale pyramid for template matching across timeframes.
 *
 * Enhancements:
 * - Expanded scale range: 0.4 to 2.5 (from 0.6-1.8) for broader coverage
 * - Adaptive stride: finer steps (0.10) near 1.0x, coarser (0.20) at extremes
 * - Pyramid caching: avoid redundant resizes for same input image
 *
 * Rationale:
 * - Timeframes change apparent candle width and pattern geometry on screen.
 * - To remain timeframe-agnostic, we search a bounded set of scales.
 * - Extreme zooms require broader scale range (0.4-2.5)
 * - Adaptive stride optimizes search density near natural scale (1.0x)
 *
 * Implementation:
 * - Build a geometric scale ladder [minScale..maxScale] with adaptive stride.
 * - Cache resized images to avoid redundant computation.
 * - Resize input up/down and run detector per scale.
 * - Keep best match per template across scales.
 *
 * Legal: Math operations only, no licensing issues.
 */
object ScaleSpace {

    data class ScaleConfig(
        val minScale: Double = 0.4,   // Expanded from 0.6 for extreme zoom out
        val maxScale: Double = 2.5,   // Expanded from 1.8 for extreme zoom in
        val fineStride: Double = 0.10,  // Fine steps near 1.0x
        val coarseStride: Double = 0.20,  // Coarse steps at extremes
        val fineRangeStart: Double = 0.8,  // Start of fine-grained region
        val fineRangeEnd: Double = 1.2   // End of fine-grained region
    )

    // Pyramid cache: Map of (imageHash, scale) -> resized Mat
    // Thread-safe cache with automatic eviction
    private val pyramidCache = ConcurrentHashMap<String, Mat>()
    private const val MAX_CACHE_ENTRIES = 1000  // Limit cache size
    private var cacheHits = 0L
    private var cacheMisses = 0L
    
    /**
     * Generate adaptive scale ladder with finer steps near 1.0x.
     * 
     * Adaptive stride rationale:
     * - Most patterns detected near natural scale (0.8-1.2x)
     * - Fine steps in this range improve accuracy
     * - Coarse steps at extremes save computation time
     * 
     * @param cfg ScaleConfig with min/max scale and stride parameters
     * @return List of scales to search, sorted ascending
     */
    fun scales(cfg: ScaleConfig): List<Double> {
        val list = mutableListOf<Double>()
        var s = cfg.minScale
        
        while (s <= cfg.maxScale + 1e-9) {
            // Quantize to 2 decimals to stay deterministic
            list.add(kotlin.math.round(s * 100) / 100.0)
            
            // Adaptive stride: fine near 1.0x, coarse at extremes
            val stride = if (s >= cfg.fineRangeStart && s <= cfg.fineRangeEnd) {
                cfg.fineStride
            } else {
                cfg.coarseStride
            }
            
            s += stride
        }
        
        // Log scale ladder on first call
        if (list.size > 0 && cacheHits == 0L && cacheMisses == 0L) {
            Timber.d("ScaleSpace: Adaptive ladder generated with ${list.size} scales (${cfg.minScale} to ${cfg.maxScale})")
        }
        
        return list
    }

    /**
     * Resize image for given scale with pyramid caching.
     * 
     * Caching strategy:
     * - Cache key: hash of (source image pointer, scale)
     * - Avoids redundant resizes when processing same image at multiple templates
     * - Automatic eviction when cache exceeds MAX_CACHE_ENTRIES
     * 
     * @param src Source Mat to resize
     * @param scale Scale factor (e.g., 1.0 = original size, 0.5 = half size, 2.0 = double size)
     * @return Resized Mat (cached if available, newly created otherwise)
     */
    fun resizeForScale(src: Mat, scale: Double): Mat {
        // Generate cache key from source pointer and scale
        val cacheKey = "${src.nativeObj}_${String.format("%.2f", scale)}"
        
        // Check cache first
        val cached = pyramidCache[cacheKey]
        if (cached != null && !cached.empty()) {
            cacheHits++
            if (cacheHits % 100L == 0L) {
                logCacheStats()
            }
            return cached.clone()  // Return clone to prevent external modification
        }
        
        cacheMisses++
        
        // Not in cache, create new resized image
        val dst = Mat()
        try {
            val newW = (src.width() * scale).toInt().coerceAtLeast(8)
            val newH = (src.height() * scale).toInt().coerceAtLeast(8)
            
            // Use INTER_AREA for downscaling (better quality), INTER_LINEAR for upscaling
            val interpolation = if (scale < 1.0) Imgproc.INTER_AREA else Imgproc.INTER_LINEAR
            
            Imgproc.resize(src, dst, org.opencv.core.Size(newW.toDouble(), newH.toDouble()), 0.0, 0.0, interpolation)
            
            // Cache the result (with size limit check)
            if (pyramidCache.size < MAX_CACHE_ENTRIES) {
                pyramidCache[cacheKey] = dst.clone()
            } else {
                // Cache full, perform emergency eviction (remove 25% oldest entries)
                evictOldestEntries()
                pyramidCache[cacheKey] = dst.clone()
            }
            
            return dst
            
        } catch (e: Exception) {
            dst.release()
            Timber.e(e, "ScaleSpace: Resize failed for scale $scale")
            throw e
        }
    }
    
    /**
     * Resize without caching (useful for one-off operations).
     * 
     * @param src Source Mat
     * @param scale Scale factor
     * @return Resized Mat
     */
    fun resizeForScaleNoCache(src: Mat, scale: Double): Mat {
        val dst = Mat()
        try {
            val newW = (src.width() * scale).toInt().coerceAtLeast(8)
            val newH = (src.height() * scale).toInt().coerceAtLeast(8)
            val interpolation = if (scale < 1.0) Imgproc.INTER_AREA else Imgproc.INTER_LINEAR
            Imgproc.resize(src, dst, org.opencv.core.Size(newW.toDouble(), newH.toDouble()), 0.0, 0.0, interpolation)
            return dst
        } catch (e: Exception) {
            dst.release()
            throw e
        }
    }
    
    /**
     * Clear pyramid cache (useful for freeing memory or reset scenarios).
     */
    fun clearCache() {
        pyramidCache.values.forEach { it.release() }
        pyramidCache.clear()
        Timber.d("ScaleSpace: Cache cleared (${cacheHits} hits, ${cacheMisses} misses)")
        cacheHits = 0L
        cacheMisses = 0L
    }
    
    /**
     * Evict oldest 25% of cache entries when cache is full.
     */
    private fun evictOldestEntries() {
        val entriesToRemove = MAX_CACHE_ENTRIES / 4
        val keys = pyramidCache.keys.toList()
        
        // Remove first N entries (FIFO approximation)
        keys.take(entriesToRemove).forEach { key ->
            pyramidCache[key]?.release()
            pyramidCache.remove(key)
        }
        
        Timber.d("ScaleSpace: Evicted $entriesToRemove cache entries")
    }
    
    /**
     * Log cache performance statistics.
     */
    private fun logCacheStats() {
        val total = cacheHits + cacheMisses
        val hitRate = if (total > 0) (cacheHits * 100.0 / total) else 0.0
        Timber.d("ScaleSpace: Cache stats - ${pyramidCache.size} entries, ${String.format("%.1f", hitRate)}% hit rate ($cacheHits/$total)")
    }
    
    /**
     * Get cache statistics.
     * 
     * @return Triple of (cacheSize, cacheHits, cacheMisses)
     */
    fun getCacheStats(): Triple<Int, Long, Long> {
        return Triple(pyramidCache.size, cacheHits, cacheMisses)
    }
}
