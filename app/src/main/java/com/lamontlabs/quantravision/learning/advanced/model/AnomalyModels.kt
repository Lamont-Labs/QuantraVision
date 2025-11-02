package com.lamontlabs.quantravision.learning.advanced.model

data class Anomaly(
    val type: AnomalyType,
    val patternType: String,
    val severity: WarningSeverity,
    val description: String,
    val timestamp: Long,
    val zScore: Float
)

enum class AnomalyType {
    SUDDEN_DROP,
    SUDDEN_IMPROVEMENT,
    UNUSUAL_STREAK,
    PERFORMANCE_SHIFT,
    OUTLIER_DETECTION
}

data class PerformanceShift(
    val patternType: String,
    val oldWinRate: Float,
    val newWinRate: Float,
    val changePercent: Float,
    val detectionTimestamp: Long,
    val likelyReason: String
)

data class AlertItem(
    val patternType: String,
    val priority: AlertPriority,
    val message: String,
    val actionRecommended: String
)

enum class AlertPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
