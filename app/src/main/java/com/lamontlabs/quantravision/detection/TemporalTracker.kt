package com.lamontlabs.quantravision.detection

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

/**
 * Temporal stability filter over a sliding window.
 * Promotes patterns that persist across scans and demotes flickers.
 */
object TemporalTracker {

    private const val HALF_LIFE_MS = 7_000L
    private val states = ConcurrentHashMap<String, State>()

    data class State(var score: Double, var lastTs: Long)

    fun update(key: String, confidence: Double, now: Long): Double {
        val s = states.getOrPut(key) { State(0.0, now) }
        // Decay since last update
        val dt = max(0L, now - s.lastTs).toDouble()
        val decay = Math.pow(0.5, dt / HALF_LIFE_MS)
        s.score = s.score * decay + confidence * (1.0 - decay)
        s.lastTs = now
        return s.score
    }
}
