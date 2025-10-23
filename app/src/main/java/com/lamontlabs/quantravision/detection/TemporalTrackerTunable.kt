package com.lamontlabs.quantravision.detection

/**
 * TemporalTrackerTunable
 * Adds runtime half-life control without altering determinism.
 */
object TemporalTrackerTunable {
    @Volatile private var halfLifeMs: Long = 7000L
    fun setHalfLife(v: Long) { halfLifeMs = v.coerceAtLeast(1000L) }
    fun getHalfLife(): Long = halfLifeMs
}
