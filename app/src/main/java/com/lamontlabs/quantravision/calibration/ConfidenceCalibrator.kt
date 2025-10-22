package com.lamontlabs.quantravision.calibration

/**
 * Simple, deterministic Platt-style calibration using fixed params per pattern.
 * In production you would learn A,B offline; here we keep a safe static map.
 */
object ConfidenceCalibrator {

    data class Params(val A: Double, val B: Double)

    // Defaults chosen conservatively to avoid false positives.
    private val params = mapOf(
        "Head & Shoulders" to Params(-1.2, 2.0),
        "Double Top" to Params(-1.0, 1.8),
        "Ascending Triangle" to Params(-1.1, 1.9)
        // Add more patterns as needed
    )

    fun calibrate(patternName: String, raw: Double): Double {
        val p = params[patternName] ?: Params(-1.0, 1.5)
        val x = raw.coerceIn(0.0, 1.0)
        val z = p.A * x + p.B
        // Sigmoid without Math.exp to preserve determinism across ABIs (use series)
        val e = seriesExp(-z)
        return 1.0 / (1.0 + e)
    }

    // 8-term Taylor for exp(t)
    private fun seriesExp(t: Double): Double {
        var sum = 1.0
        var term = 1.0
        for (k in 1..8) {
            term *= t / k
            sum += term
        }
        return sum
    }
}
