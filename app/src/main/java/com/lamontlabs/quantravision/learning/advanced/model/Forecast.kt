package com.lamontlabs.quantravision.learning.advanced.model

data class Forecast(
    val predictedWinRate: Float,
    val confidenceIntervalUpper: Float,
    val confidenceIntervalLower: Float,
    val trendDirection: TrendDirection,
    val confidence: Float
)

enum class TrendDirection {
    IMPROVING,
    DECLINING,
    STABLE
}

data class TrendWarning(
    val patternType: String,
    val severity: WarningSeverity,
    val message: String,
    val currentTrend: TrendDirection
)

enum class WarningSeverity {
    INFO,
    WARNING,
    CRITICAL
}
