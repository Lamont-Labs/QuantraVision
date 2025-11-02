package com.lamontlabs.quantravision.learning.advanced.model

data class RiskMetrics(
    val sharpeRatio: Float,
    val expectedValue: Float,
    val volatility: Float,
    val maxDrawdown: Float,
    val recoveryTime: Long
)

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class RankedPattern(
    val patternType: String,
    val sharpeRatio: Float,
    val expectedValue: Float,
    val winRate: Float,
    val sampleSize: Int
)
