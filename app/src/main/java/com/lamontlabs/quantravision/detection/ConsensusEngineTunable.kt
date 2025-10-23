package com.lamontlabs.quantravision.detection

/**
 * ConsensusEngineTunable
 * Runtime-tunable sigma for scale proximity weighting.
 */
object ConsensusEngineTunable {
    @Volatile private var sigma: Double = 0.2
    fun setSigma(v: Double) { sigma = v.coerceIn(0.05, 1.0) }
    fun getSigma(): Double = sigma
}
