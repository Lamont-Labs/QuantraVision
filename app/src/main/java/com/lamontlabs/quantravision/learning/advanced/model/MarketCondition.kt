package com.lamontlabs.quantravision.learning.advanced.model

import com.lamontlabs.quantravision.regime.RegimeNavigator

enum class MarketCondition {
    VOLATILE_TRENDING,
    VOLATILE_RANGING,
    CALM_TRENDING,
    CALM_RANGING,
    NEUTRAL;

    companion object {
        fun fromRegime(
            volatility: RegimeNavigator.VolatilityLevel,
            trendStrength: RegimeNavigator.TrendStrength
        ): MarketCondition {
            val isVolatile = volatility == RegimeNavigator.VolatilityLevel.HIGH
            val isTrending = trendStrength != RegimeNavigator.TrendStrength.WEAK

            return when {
                isVolatile && isTrending -> VOLATILE_TRENDING
                isVolatile && !isTrending -> VOLATILE_RANGING
                !isVolatile && isTrending -> CALM_TRENDING
                !isVolatile && !isTrending -> CALM_RANGING
                else -> NEUTRAL
            }
        }
    }
}

data class ConditionBreakdown(
    val condition: MarketCondition,
    val winRate: Float,
    val sampleSize: Int,
    val recommendationStrength: RecommendationStrength
)

enum class RecommendationStrength {
    STRONG_BUY,
    BUY,
    NEUTRAL,
    AVOID,
    STRONG_AVOID
}
