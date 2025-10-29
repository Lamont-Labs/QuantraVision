package com.lamontlabs.quantravision.detection

import com.lamontlabs.quantravision.PatternDetector
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Multi-template consensus with deterministic weighting.
 * Inputs: list of ScaleMatch for a single template family (same patternName).
 * Output: consensus score [0..1] favoring agreement across nearby scales.
 */
object ConsensusEngine {

    data class ConsensusResult(
        val patternName: String,
        val consensusScore: Double,
        val bestConfidence: Double,
        val bestScale: Double
    )

    fun compute(patternName: String, matches: List<PatternDetector.ScaleMatch>): ConsensusResult? {
        if (matches.isEmpty()) return null
        // Weight by confidence and proximity to median scale
        val scales = matches.map { it.scale }.sorted()
        val median = scales[scales.size / 2]
        var num = 0.0
        var den = 0.0
        var best = matches.maxBy { it.confidence }
        matches.forEach {
            val prox = gaussianProximity(it.scale, median, sigma = 0.2)
            val w = prox
            num += w * it.confidence
            den += w
        }
        val consensus = if (den == 0.0) 0.0 else num / den
        return ConsensusResult(patternName, consensus, best.confidence, best.scale)
    }

    private fun gaussianProximity(x: Double, mu: Double, sigma: Double): Double {
        if (sigma == 0.0) return 0.0
        val z = (x - mu) / sigma
        // exp(-0.5*z^2) without kotlin.math.exp to keep deterministic across runtimes
        // Use 5th-order Taylor for exp approximation:
        val t = -0.5 * z * z
        return 1.0 + t + t*t/2.0 + t*t*t/6.0 + t.pow(4)/24.0 + t.pow(5)/120.0
    }
}
