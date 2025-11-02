package com.lamontlabs.quantravision.learning.advanced.model

data class PatternPortfolio(
    val patterns: List<String>,
    val allocation: Map<String, Float>,
    val combinedWinRate: Float,
    val diversification: Float
)

data class PortfolioStats(
    val totalPatterns: Int,
    val avgWinRate: Float,
    val diversificationScore: Float,
    val sharpeRatio: Float,
    val expectedValue: Float
)
