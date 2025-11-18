package com.lamontlabs.quantravision.intelligence.llm

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory + persistent cache for LLM explanations
 * 
 * Caching strategy:
 * - In-memory LRU cache for fast access
 * - SharedPreferences for persistence across app restarts
 * - TTL-based expiration (24 hours by default)
 * 
 * Reduces inference costs and improves response time for repeated patterns.
 */
class ExplanationCache(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "llm_explanation_cache",
        Context.MODE_PRIVATE
    )
    
    // In-memory cache with LRU eviction
    private val memoryCache = object : LinkedHashMap<String, CacheEntry>(
        ModelConfig.MAX_CACHE_SIZE,
        0.75f,
        true  // Access order (LRU)
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean {
            return size > ModelConfig.MAX_CACHE_SIZE
        }
    }
    
    private data class CacheEntry(
        val text: String,
        val timestamp: Long
    )
    
    /**
     * Get explanation from cache if not expired
     */
    fun get(key: String): String? {
        // Check memory cache first
        memoryCache[key]?.let { entry ->
            if (!isExpired(entry.timestamp)) {
                return entry.text
            } else {
                memoryCache.remove(key)
            }
        }
        
        // Check persistent cache
        val persistedEntry = prefs.getString(key, null)
        if (persistedEntry != null) {
            val parts = persistedEntry.split("|", limit = 2)
            if (parts.size == 2) {
                val timestamp = parts[0].toLongOrNull() ?: 0L
                val text = parts[1]
                
                if (!isExpired(timestamp)) {
                    // Restore to memory cache
                    memoryCache[key] = CacheEntry(text, timestamp)
                    return text
                } else {
                    // Remove expired entry
                    prefs.edit().remove(key).apply()
                }
            }
        }
        
        return null
    }
    
    /**
     * Store explanation in cache
     */
    fun put(key: String, text: String) {
        val timestamp = System.currentTimeMillis()
        val entry = CacheEntry(text, timestamp)
        
        // Store in memory
        memoryCache[key] = entry
        
        // Persist to disk
        if (ModelConfig.CACHE_EXPLANATIONS) {
            prefs.edit()
                .putString(key, "$timestamp|$text")
                .apply()
        }
        
        Timber.d("🧠 Cached explanation: $key")
    }
    
    /**
     * Clear all cached explanations
     */
    fun clear() {
        memoryCache.clear()
        prefs.edit().clear().apply()
        Timber.i("🧠 Explanation cache cleared")
    }
    
    /**
     * Check if cache entry is expired
     */
    private fun isExpired(timestamp: Long): Boolean {
        val ageHours = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60)
        return ageHours > ModelConfig.CACHE_TTL_HOURS
    }
    
    /**
     * Get cache statistics
     */
    fun getStats(): CacheStats {
        val totalEntries = prefs.all.size
        val memoryEntries = memoryCache.size
        
        return CacheStats(
            totalEntries = totalEntries,
            memoryEntries = memoryEntries,
            hitRate = 0.0  // TODO: Track hit/miss ratio
        )
    }
    
    data class CacheStats(
        val totalEntries: Int,
        val memoryEntries: Int,
        val hitRate: Double
    )
}
