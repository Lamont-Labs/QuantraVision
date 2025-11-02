package com.lamontlabs.quantravision.cache

import com.lamontlabs.quantravision.PatternMatch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache for detection results with TTL support.
 * Avoids redundant detections for static chart regions.
 */
class DetectionCache(
    private val ttlMs: Long = 5000L,  // 5 seconds TTL
    private val maxEntries: Int = 100
) {
    
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    
    data class CacheEntry(
        val patterns: List<PatternMatch>,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttl: Long): Boolean {
            return (System.currentTimeMillis() - timestamp) > ttl
        }
    }
    
    data class CacheStats(
        val size: Int,
        val hits: Long,
        val misses: Long,
        val expirations: Long,
        val hitRate: Double,
        val memoryUsageKB: Double
    )
    
    private var cacheHits = 0L
    private var cacheMisses = 0L
    private var cacheExpirations = 0L
    
    /**
     * Get cached detection results if available and not expired.
     * 
     * @param chartId Unique identifier for the chart
     * @return Cached patterns or null if not cached/expired
     */
    fun get(chartId: String): List<PatternMatch>? {
        val entry = cache[chartId]
        
        if (entry == null) {
            cacheMisses++
            return null
        }
        
        if (entry.isExpired(ttlMs)) {
            // Expired, remove from cache
            cache.remove(chartId)
            cacheExpirations++
            cacheMisses++
            return null
        }
        
        // Valid cache hit
        cacheHits++
        return entry.patterns
    }
    
    /**
     * Store detection results in cache.
     * 
     * @param chartId Unique identifier for the chart
     * @param patterns Detected patterns to cache
     */
    fun put(chartId: String, patterns: List<PatternMatch>) {
        // Enforce max size with LRU eviction
        if (cache.size >= maxEntries) {
            evictOldest()
        }
        
        val entry = CacheEntry(patterns = patterns)
        cache[chartId] = entry
        
        Timber.d("Cached ${patterns.size} patterns for chart $chartId")
    }
    
    /**
     * Invalidate cache entry for specific chart.
     * 
     * @param chartId Chart identifier
     */
    fun invalidate(chartId: String) {
        cache.remove(chartId)
        Timber.d("Invalidated cache for chart $chartId")
    }
    
    /**
     * Clear all cached entries.
     */
    fun clear() {
        val size = cache.size
        cache.clear()
        Timber.d("Cleared detection cache ($size entries)")
    }
    
    /**
     * Remove expired entries.
     */
    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        val toRemove = mutableListOf<String>()
        
        cache.forEach { (key, entry) ->
            if (entry.isExpired(ttlMs)) {
                toRemove.add(key)
            }
        }
        
        toRemove.forEach { key ->
            cache.remove(key)
            cacheExpirations++
        }
        
        if (toRemove.isNotEmpty()) {
            Timber.d("Cleaned up ${toRemove.size} expired cache entries")
        }
    }
    
    /**
     * Evict oldest entry (LRU).
     */
    private fun evictOldest() {
        val oldest = cache.entries.minByOrNull { it.value.timestamp }
        oldest?.let {
            cache.remove(it.key)
            Timber.d("Evicted oldest cache entry: ${it.key}")
        }
    }
    
    /**
     * Get cache statistics.
     */
    fun getStats(): CacheStats {
        // Clean up expired entries first
        cleanupExpired()
        
        val total = cacheHits + cacheMisses
        val hitRate = if (total > 0) {
            cacheHits.toDouble() / total
        } else {
            0.0
        }
        
        // Estimate memory usage
        val avgPatternsPerEntry = cache.values.map { it.patterns.size }.average()
        val estimatedBytesPerPattern = 200  // Rough estimate
        val memoryUsageBytes = cache.size * avgPatternsPerEntry * estimatedBytesPerPattern
        val memoryUsageKB = memoryUsageBytes / 1024.0
        
        return CacheStats(
            size = cache.size,
            hits = cacheHits,
            misses = cacheMisses,
            expirations = cacheExpirations,
            hitRate = hitRate,
            memoryUsageKB = memoryUsageKB
        )
    }
    
    /**
     * Log cache statistics.
     */
    fun logStats() {
        val stats = getStats()
        Timber.i(
            "DetectionCache Stats:\n" +
            "  Size: ${stats.size}/${maxEntries}\n" +
            "  Hit Rate: ${String.format("%.1f", stats.hitRate * 100)}%\n" +
            "  Hits: ${stats.hits}, Misses: ${stats.misses}\n" +
            "  Expirations: ${stats.expirations}\n" +
            "  Memory: ${String.format("%.2f", stats.memoryUsageKB)} KB"
        )
    }
    
    /**
     * Reset statistics.
     */
    fun resetStats() {
        cacheHits = 0L
        cacheMisses = 0L
        cacheExpirations = 0L
    }
}
