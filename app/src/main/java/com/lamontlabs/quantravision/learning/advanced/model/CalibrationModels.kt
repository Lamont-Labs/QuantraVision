package com.lamontlabs.quantravision.learning.advanced.model

data class OptimalThreshold(
    val patternType: String,
    val threshold: Float,
    val truePositiveRate: Float,
    val falsePositiveRate: Float,
    val f1Score: Float
)

enum class ConvergenceStatus {
    CONVERGED,
    IMPROVING,
    STUCK,
    INSUFFICIENT_DATA
}

data class CalibrationResult(
    val finalLoss: Float,
    val iterations: Int,
    val convergenceStatus: ConvergenceStatus,
    val optimalThresholds: Map<String, OptimalThreshold>
)
