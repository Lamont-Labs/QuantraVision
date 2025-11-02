package com.lamontlabs.quantravision.learning.advanced.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class OvertradingAnalysis(
    val patternsPerHour: Float,
    val normalRate: Float,
    val impactOnWinRate: Float,
    val isOvertrading: Boolean
)

data class RevengePattern(
    val detectedPostLoss: Boolean,
    val avgTimeToNextTrade: Duration,
    val normalTime: Duration,
    val postLossWinRate: Float,
    val normalWinRate: Float
)

data class BehavioralWarning(
    val type: BehavioralWarningType,
    val severity: WarningSeverity,
    val message: String,
    val recommendation: String
)

enum class BehavioralWarningType {
    OVERTRADING,
    REVENGE_TRADING,
    FATIGUE,
    CONSISTENCY
}

data class SessionAnalysis(
    val optimalDuration: Duration = 2.5.hours,
    val currentDuration: Duration,
    val avgSuccessByHour: Map<Int, Float>
)
