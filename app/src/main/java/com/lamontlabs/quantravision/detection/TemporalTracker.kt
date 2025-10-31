package com.lamontlabs.quantravision.detection

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

/**
 * Temporal stability filter over a sliding window.
 * Promotes patterns that persist across scans and demotes flickers.
 * Includes eviction policy to prevent unbounded memory growth.
 */
object TemporalTracker {

    private const val HALF_LIFE_MS = 7_000L
    private const val MAX_ENTRIES = 10_000  // Maximum cached entries
    private const val EVICTION_AGE_MS = 300_000L  // Evict entries older than 5 minutes
    private const val EVICTION_CHECK_INTERVAL = 100  // Check for eviction every N updates
    
    private val states = ConcurrentHashMap<String, State>()
    private var updateCount = 0

    data class State(var score: Double, var lastTs: Long)

    fun update(key: String, confidence: Double, now: Long): Double {
        val s = states.getOrPut(key) { State(0.0, now) }
        // Decay since last update
        val dt = max(0L, now - s.lastTs).toDouble()
        val decay = Math.pow(0.5, dt / HALF_LIFE_MS)
        s.score = s.score * decay + confidence * (1.0 - decay)
        s.lastTs = now
        
        // Periodic eviction to prevent memory leaks
        updateCount++
        if (updateCount % EVICTION_CHECK_INTERVAL == 0) {
            evictStaleEntries(now)
        }
        
        return s.score
    }
    
    /**
     * Evict old entries to prevent unbounded memory growth.
     * Removes entries that haven't been updated in EVICTION_AGE_MS.
     */
    private fun evictStaleEntries(now: Long) {
        val cutoffTime = now - EVICTION_AGE_MS
        val keysToRemove = states.keys.filter { key ->
            val state = states[key]
            state != null && state.lastTs < cutoffTime
        }
        
        keysToRemove.forEach { states.remove(it) }
        
        // Emergency size-based eviction if we exceed max entries
        if (states.size > MAX_ENTRIES) {
            val entriesToRemove = states.size - (MAX_ENTRIES * 3 / 4)  // Remove 25% when full
            val oldestEntries = states.entries
                .sortedBy { it.value.lastTs }
                .take(entriesToRemove)
            
            oldestEntries.forEach { states.remove(it.key) }
        }
    }
    
    /**
     * Manually clear all state (for testing or reset scenarios).
     */
    fun clear() {
        states.clear()
        updateCount = 0
    }
    
    /**
     * Get current state count (for monitoring).
     */
    fun size(): Int = states.size
}
