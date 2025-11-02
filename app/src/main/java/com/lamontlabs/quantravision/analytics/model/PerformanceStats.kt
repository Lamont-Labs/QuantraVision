package com.lamontlabs.quantravision.analytics.model

data class WinRateStats(
    val patternName: String,
    val totalOutcomes: Int,
    val wins: Int,
    val losses: Int,
    val neutral: Int,
    val winRate: Double,
    val avgProfitLoss: Double = 0.0
) {
    val lossRate: Double get() = if (totalOutcomes > 0) losses.toDouble() / totalOutcomes else 0.0
    val neutralRate: Double get() = if (totalOutcomes > 0) neutral.toDouble() / totalOutcomes else 0.0
}

data class PatternFrequency(
    val patternName: String,
    val detectionCount: Int,
    val lastDetected: Long,
    val avgConfidence: Double,
    val timeframeDistribution: Map<String, Int>
)

data class TimeOfDayStats(
    val hourOfDay: Int,
    val detectionCount: Int,
    val winRate: Double,
    val avgConfidence: Double
) {
    val timeLabel: String get() = String.format("%02d:00", hourOfDay)
}

data class OverallPerformanceStats(
    val totalPatterns: Int,
    val totalOutcomes: Int,
    val overallWinRate: Double,
    val bestPatterns: List<WinRateStats>,
    val worstPatterns: List<WinRateStats>,
    val frequencyStats: List<PatternFrequency>,
    val timeOfDayStats: List<TimeOfDayStats>
)
