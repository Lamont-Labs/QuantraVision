package com.lamontlabs.quantravision.roi

/**
 * ROIProposerTunable
 * Controls the cap on proposed ROIs for performance.
 */
object ROIProposerTunable {
    @Volatile private var maxRois: Int = 8
    fun setMaxRois(n: Int) { maxRois = n.coerceIn(1, 64) }
    fun getMaxRois(): Int = maxRois
}
