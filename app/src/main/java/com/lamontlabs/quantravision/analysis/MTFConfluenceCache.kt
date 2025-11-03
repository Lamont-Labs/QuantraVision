package com.lamontlabs.quantravision.analysis

import com.lamontlabs.quantravision.detection.Detection
import java.util.concurrent.ConcurrentHashMap

/**
 * Multi-Time-Frame Confluence Cache
 * - Holds recent detections from up to three timeframes
 * - Produces a normalized confluence score
 * - Auto-expires after 90 s to stay deterministic and low-memory
 */
class MTFConfluenceCache {
    private val store = ConcurrentHashMap<String,Entry>()
    private val ttlMs = 90_000L

    fun add(tf:String, dets:List<Detection>) {
        store[tf] = Entry(System.currentTimeMillis(), dets)
        purgeOld()
    }

    fun confluenceScore(): Float {
        if (store.isEmpty()) return 0f
        val all = store.values.map { it.dets.map { d -> d.name } }.flatten()
        if (all.isEmpty()) return 0f
        val unique = all.toSet()
        val overlap = if (unique.isEmpty()) 0f else (all.size - unique.size).toFloat() / all.size
        return (overlap * 0.7f + store.size / 3f * 0.3f).coerceIn(0f,1f)
    }

    fun agreeingLabels(): List<String> {
        val grouped = store.values.flatMap { it.dets }.groupBy { it.name }
        return grouped.filter { it.value.size > 1 }.keys.toList()
    }

    private fun purgeOld() {
        val now = System.currentTimeMillis()
        store.entries.removeIf { now - it.value.ts > ttlMs }
    }

    private data class Entry(val ts:Long,val dets:List<Detection>)
}
