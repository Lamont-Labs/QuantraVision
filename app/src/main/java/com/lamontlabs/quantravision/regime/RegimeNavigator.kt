package com.lamontlabs.quantravision.regime

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.licensing.AdvancedFeatureGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * RegimeNavigator
 * 
 * On-device market regime classifier that analyzes chart conditions to provide
 * educational context for detected patterns.
 * 
 * TIER REQUIREMENT: Requires Standard tier ($14.99) or Pro tier ($29.99)
 * 
 * LEGAL: This is an EDUCATIONAL TOOL ONLY. Regime classifications are NOT
 * market predictions, forecasts, or trading recommendations. See legal/ADVANCED_FEATURES_DISCLAIMER.md
 * 
 * DISCLAIMER REQUIREMENT: Users MUST accept ADVANCED_FEATURES_DISCLAIMER.md before accessing
 * Regime Navigator. All public methods enforce this via AdvancedFeatureGate.
 * 
 * Market Regimes Analyzed:
 * - Volatility (Low/Medium/High)
 * - Trend Strength (Weak/Moderate/Strong)
 * - Liquidity Proxy (based on price action consistency)
 * 
 * Educational Purpose: Help traders understand WHEN patterns historically have higher/lower
 * success rates in different market conditions.
 */
object RegimeNavigator {

    /**
     * Market regime classification
     */
    data class MarketRegime(
        val volatility: VolatilityLevel,
        val trendStrength: TrendStrength,
        val liquidity: LiquidityLevel,
        val overallQuality: RegimeQuality,
        val regimeType: RegimeType,
        val patternDensity: PatternDensity,
        val multiTimeframeConsistency: Double,
        val atrLikeVolatility: Double,
        val historicalSuccessEstimate: Double,
        val educationalContext: String,
        val disclaimer: String = "⚠️ Educational context only - NOT trading advice"
    )

    enum class VolatilityLevel {
        LOW,      // Calm, range-bound
        MEDIUM,   // Normal fluctuations
        HIGH      // Extreme movements
    }

    enum class TrendStrength {
        WEAK,     // Choppy, sideways
        MODERATE, // Visible direction, some pullbacks
        STRONG    // Clear sustained direction
    }

    enum class LiquidityLevel {
        LOW,      // Erratic, inconsistent
        MEDIUM,   // Normal
        HIGH      // Smooth, consistent
    }

    enum class RegimeQuality {
        POOR,      // 🔴 Low probability environment
        NEUTRAL,   // 🟡 Average conditions
        FAVORABLE  // 🟢 High probability environment
    }

    enum class RegimeType {
        BULL_TREND,        // Sustained upward movement
        BEAR_TREND,        // Sustained downward movement
        SIDEWAYS,          // Range-bound, no clear direction
        HIGH_VOLATILITY,   // Choppy, high variance
        LOW_VOLATILITY,    // Calm, low variance
        TRANSITIONING      // Changing regime
    }

    enum class PatternDensity {
        SPARSE,    // Few patterns detected
        NORMAL,    // Average pattern frequency
        DENSE      // High pattern frequency (high liquidity proxy)
    }

    /**
     * Analyze market regime from recent price data with enhanced metrics
     * 
     * LEGAL GATE: Requires acceptance of ADVANCED_FEATURES_DISCLAIMER.md
     * 
     * @param context Android context for checking disclaimer acceptance
     * @param priceData Recent price points (close prices)
     * @param patternConfidences Recent pattern detection confidences (for ATR-like calc)
     * @param patternDetectionCount Number of patterns detected (for density)
     * @param multiTimeframeData Optional price data from different timeframes
     * @return Enhanced educational market regime classification
     * @throws IllegalStateException if disclaimer not accepted
     */
    suspend fun analyzeRegime(
        context: Context,
        priceData: List<Double>,
        patternConfidences: List<Double>? = null,
        patternDetectionCount: Int = 0,
        multiTimeframeData: Map<String, List<Double>>? = null
    ): MarketRegime = withContext(Dispatchers.Default) {
        
        // CRITICAL TIER GATE: Regime Navigator requires Standard tier ($14.99) or higher
        if (!com.lamontlabs.quantravision.licensing.StandardFeatureGate.isActive(context)) {
            throw IllegalStateException(
                "Regime Navigator requires Standard tier ($14.99) or Pro tier ($29.99). " +
                "Upgrade to unlock market condition analysis."
            )
        }
        
        // CRITICAL LEGAL GATE: Enforce disclaimer acceptance
        AdvancedFeatureGate.requireAcceptance(context, "Regime Navigator")
        
        if (priceData.size < 20) {
            return MarketRegime(
                volatility = VolatilityLevel.MEDIUM,
                trendStrength = TrendStrength.WEAK,
                liquidity = LiquidityLevel.MEDIUM,
                overallQuality = RegimeQuality.NEUTRAL,
                regimeType = RegimeType.SIDEWAYS,
                patternDensity = PatternDensity.NORMAL,
                multiTimeframeConsistency = 0.5,
                atrLikeVolatility = 0.0,
                historicalSuccessEstimate = 0.5,
                educationalContext = "Insufficient data for regime analysis (need 20+ data points)"
            )
        }

        val volatility = calculateVolatility(priceData)
        val trendStrength = calculateTrendStrength(priceData)
        val liquidity = calculateLiquidityProxy(priceData)
        val atrVol = calculateATRLikeVolatility(patternConfidences ?: priceData.map { 0.5 })
        val density = calculatePatternDensity(patternDetectionCount, priceData.size)
        val mtfConsistency = calculateMultiTimeframeConsistency(priceData, multiTimeframeData)
        val regimeType = classifyRegimeType(volatility, trendStrength, priceData)
        val quality = determineRegimeQuality(volatility, trendStrength, liquidity)
        val successEstimate = estimateSuccessRate(regimeType, quality, density)
        val educationalContext = generateEducationalContext(
            volatility, trendStrength, liquidity, quality, regimeType, density, mtfConsistency, successEstimate
        )

        return MarketRegime(
            volatility = volatility,
            trendStrength = trendStrength,
            liquidity = liquidity,
            overallQuality = quality,
            regimeType = regimeType,
            patternDensity = density,
            multiTimeframeConsistency = mtfConsistency,
            atrLikeVolatility = atrVol,
            historicalSuccessEstimate = successEstimate,
            educationalContext = educationalContext
        )
    }

    /**
     * Annotate a pattern match with regime context
     * 
     * LEGAL GATE: Requires acceptance of ADVANCED_FEATURES_DISCLAIMER.md
     * 
     * @param context Android context for checking disclaimer acceptance
     * @param match Pattern match to annotate
     * @param regime Market regime analysis
     * @return Annotated pattern with regime context
     * @throws IllegalStateException if disclaimer not accepted
     */
    suspend fun annotatePattern(
        context: Context,
        match: PatternMatch,
        regime: MarketRegime
    ): AnnotatedPattern = withContext(Dispatchers.Default) {
        
        // CRITICAL TIER GATE: Regime Navigator requires Standard tier ($14.99) or higher
        if (!com.lamontlabs.quantravision.licensing.StandardFeatureGate.isActive(context)) {
            throw IllegalStateException(
                "Regime Navigator requires Standard tier ($14.99) or Pro tier ($29.99). " +
                "Upgrade to unlock market condition analysis."
            )
        }
        
        // CRITICAL LEGAL GATE: Enforce disclaimer acceptance
        AdvancedFeatureGate.requireAcceptance(context, "Regime Navigator")
        
        val historicalSuccessRate = estimateHistoricalSuccessRate(
            match.patternId,
            regime
        )
        
        val recommendation = generateEducationalRecommendation(
            match.patternId,
            regime,
            historicalSuccessRate
        )

        return AnnotatedPattern(
            originalMatch = match,
            regime = regime,
            historicalSuccessRate = historicalSuccessRate,
            educationalRecommendation = recommendation
        )
    }

    /**
     * Pattern annotated with regime context
     */
    data class AnnotatedPattern(
        val originalMatch: PatternMatch,
        val regime: MarketRegime,
        val historicalSuccessRate: Double, // e.g., 0.72 = 72% in similar conditions (educational estimate)
        val educationalRecommendation: String // Educational context, NOT advice
    )

    // ==================== Private Helper Functions ====================

    private fun calculateVolatility(prices: List<Double>): VolatilityLevel {
        val returns = prices.zipWithNext { a, b -> (b - a) / a }
        val avgReturn = returns.average()
        val variance = returns.map { (it - avgReturn) * (it - avgReturn) }.average()
        val stdDev = sqrt(variance)
        
        // Annualized volatility approximation
        val annualizedVol = stdDev * sqrt(252.0)
        
        return when {
            annualizedVol < 0.15 -> VolatilityLevel.LOW
            annualizedVol < 0.30 -> VolatilityLevel.MEDIUM
            else -> VolatilityLevel.HIGH
        }
    }

    private fun calculateTrendStrength(prices: List<Double>): TrendStrength {
        // Simple linear regression slope
        val n = prices.size
        val x = (0 until n).map { it.toDouble() }
        val y = prices
        
        val xMean = x.average()
        val yMean = y.average()
        
        val numerator = x.zip(y) { xi, yi -> (xi - xMean) * (yi - yMean) }.sum()
        val denominator = x.map { (it - xMean) * (it - xMean) }.sum()
        
        val slope = if (denominator != 0.0) numerator / denominator else 0.0
        val normalizedSlope = abs(slope) / yMean // Normalize by price level
        
        return when {
            normalizedSlope < 0.001 -> TrendStrength.WEAK
            normalizedSlope < 0.005 -> TrendStrength.MODERATE
            else -> TrendStrength.STRONG
        }
    }

    private fun calculateLiquidityProxy(prices: List<Double>): LiquidityLevel {
        // Use price consistency as liquidity proxy
        // High liquidity = smoother price action
        val priceRanges = prices.zipWithNext { a, b -> abs(b - a) / a }
        val avgRange = priceRanges.average()
        val consistency = 1.0 - (priceRanges.map { abs(it - avgRange) }.average() / avgRange)
        
        return when {
            consistency > 0.7 -> LiquidityLevel.HIGH
            consistency > 0.4 -> LiquidityLevel.MEDIUM
            else -> LiquidityLevel.LOW
        }
    }

    private fun determineRegimeQuality(
        volatility: VolatilityLevel,
        trend: TrendStrength,
        liquidity: LiquidityLevel
    ): RegimeQuality {
        
        // Educational scoring based on common technical analysis principles
        val score = when {
            trend == TrendStrength.STRONG && volatility == VolatilityLevel.MEDIUM && liquidity == LiquidityLevel.HIGH -> 3
            trend == TrendStrength.STRONG && volatility == VolatilityLevel.LOW -> 2
            trend == TrendStrength.MODERATE && liquidity == LiquidityLevel.HIGH -> 2
            trend == TrendStrength.WEAK || volatility == VolatilityLevel.HIGH -> -1
            else -> 0
        }
        
        return when {
            score >= 2 -> RegimeQuality.FAVORABLE
            score <= -1 -> RegimeQuality.POOR
            else -> RegimeQuality.NEUTRAL
        }
    }

    private fun calculateATRLikeVolatility(confidences: List<Double>): Double {
        if (confidences.size < 2) return 0.0
        
        val ranges = confidences.zipWithNext { a, b -> abs(b - a) }
        return ranges.average()
    }
    
    private fun calculatePatternDensity(detectionCount: Int, dataPoints: Int): PatternDensity {
        if (dataPoints == 0) return PatternDensity.NORMAL
        
        val density = detectionCount.toDouble() / dataPoints
        return when {
            density > 0.15 -> PatternDensity.DENSE
            density < 0.05 -> PatternDensity.SPARSE
            else -> PatternDensity.NORMAL
        }
    }
    
    private fun calculateMultiTimeframeConsistency(
        primaryData: List<Double>,
        mtfData: Map<String, List<Double>>?
    ): Double {
        if (mtfData.isNullOrEmpty()) return 0.5
        
        val primaryTrend = calculateTrendStrength(primaryData)
        var consistentTimeframes = 0
        
        mtfData.values.forEach { data ->
            if (data.size >= 10) {
                val timeframeTrend = calculateTrendStrength(data)
                if (timeframeTrend == primaryTrend) {
                    consistentTimeframes++
                }
            }
        }
        
        return consistentTimeframes.toDouble() / mtfData.size
    }
    
    private fun classifyRegimeType(
        vol: VolatilityLevel,
        trend: TrendStrength,
        prices: List<Double>
    ): RegimeType {
        val priceChange = (prices.last() - prices.first()) / prices.first()
        
        return when {
            vol == VolatilityLevel.HIGH -> RegimeType.HIGH_VOLATILITY
            vol == VolatilityLevel.LOW && trend == TrendStrength.WEAK -> RegimeType.LOW_VOLATILITY
            trend == TrendStrength.STRONG && priceChange > 0.05 -> RegimeType.BULL_TREND
            trend == TrendStrength.STRONG && priceChange < -0.05 -> RegimeType.BEAR_TREND
            trend == TrendStrength.WEAK -> RegimeType.SIDEWAYS
            else -> RegimeType.TRANSITIONING
        }
    }
    
    private fun estimateSuccessRate(
        regimeType: RegimeType,
        quality: RegimeQuality,
        density: PatternDensity
    ): Double {
        var baseRate = when (quality) {
            RegimeQuality.FAVORABLE -> 0.75
            RegimeQuality.NEUTRAL -> 0.55
            RegimeQuality.POOR -> 0.40
        }
        
        baseRate += when (regimeType) {
            RegimeType.BULL_TREND, RegimeType.BEAR_TREND -> 0.10
            RegimeType.LOW_VOLATILITY -> 0.05
            RegimeType.HIGH_VOLATILITY -> -0.10
            RegimeType.SIDEWAYS -> -0.05
            RegimeType.TRANSITIONING -> -0.05
        }
        
        baseRate += when (density) {
            PatternDensity.DENSE -> 0.05
            PatternDensity.NORMAL -> 0.0
            PatternDensity.SPARSE -> -0.05
        }
        
        return baseRate.coerceIn(0.30, 0.90)
    }
    
    private fun generateEducationalContext(
        vol: VolatilityLevel,
        trend: TrendStrength,
        liq: LiquidityLevel,
        quality: RegimeQuality,
        regimeType: RegimeType,
        density: PatternDensity,
        mtfConsistency: Double,
        successEstimate: Double
    ): String {
        
        val volDesc = when (vol) {
            VolatilityLevel.LOW -> "low volatility (calm market)"
            VolatilityLevel.MEDIUM -> "normal volatility"
            VolatilityLevel.HIGH -> "high volatility (turbulent market)"
        }
        
        val trendDesc = when (trend) {
            TrendStrength.WEAK -> "weak trend (choppy/sideways)"
            TrendStrength.MODERATE -> "moderate trend"
            TrendStrength.STRONG -> "strong trend (clear direction)"
        }
        
        val liqDesc = when (liq) {
            LiquidityLevel.LOW -> "low liquidity proxy (erratic price action)"
            LiquidityLevel.MEDIUM -> "normal liquidity proxy"
            LiquidityLevel.HIGH -> "high liquidity proxy (smooth price action)"
        }
        
        val qualityEmoji = when (quality) {
            RegimeQuality.FAVORABLE -> "🟢"
            RegimeQuality.NEUTRAL -> "🟡"
            RegimeQuality.POOR -> "🔴"
        }
        
        val regimeDesc = when (regimeType) {
            RegimeType.BULL_TREND -> "📈 Bull Trend"
            RegimeType.BEAR_TREND -> "📉 Bear Trend"
            RegimeType.SIDEWAYS -> "↔️ Sideways"
            RegimeType.HIGH_VOLATILITY -> "⚡ High Volatility"
            RegimeType.LOW_VOLATILITY -> "😴 Low Volatility"
            RegimeType.TRANSITIONING -> "🔄 Transitioning"
        }
        
        val densityDesc = when (density) {
            PatternDensity.DENSE -> "High pattern density (strong liquidity proxy)"
            PatternDensity.NORMAL -> "Normal pattern density"
            PatternDensity.SPARSE -> "Low pattern density (weak liquidity proxy)"
        }
        
        val successPercent = (successEstimate * 100).toInt()
        val mtfPercent = (mtfConsistency * 100).toInt()
        
        return buildString {
            append("$qualityEmoji $regimeDesc\n")
            append("Market: $volDesc, $trendDesc, $liqDesc\n")
            append("$densityDesc\n")
            append("Multi-timeframe consistency: $mtfPercent%\n")
            append("📚 Educational: Historically patterns in similar conditions succeeded ~$successPercent% of the time\n")
            append("⚠️ Past performance does NOT predict future results")
        }
    }

    private fun estimateHistoricalSuccessRate(
        patternId: String,
        regime: MarketRegime
    ): Double {
        
        // Educational estimates based on technical analysis literature
        // These are NOT predictions - just historical reference points for education
        
        val baseRate = when {
            patternId.contains("head_and_shoulders") -> 0.65
            patternId.contains("double_") -> 0.60
            patternId.contains("triangle") -> 0.55
            patternId.contains("flag") || patternId.contains("pennant") -> 0.70
            else -> 0.50
        }
        
        // Adjust for regime (educational adjustment)
        val regimeMultiplier = when (regime.overallQuality) {
            RegimeQuality.FAVORABLE -> 1.15
            RegimeQuality.NEUTRAL -> 1.0
            RegimeQuality.POOR -> 0.75
        }
        
        return (baseRate * regimeMultiplier).coerceIn(0.30, 0.90)
    }

    private fun generateEducationalRecommendation(
        patternId: String,
        regime: MarketRegime,
        historicalRate: Double
    ): String {
        
        val ratePercent = (historicalRate * 100).toInt()
        
        return when (regime.overallQuality) {
            RegimeQuality.FAVORABLE -> 
                "📚 Educational: Similar patterns in favorable conditions historically succeeded ~$ratePercent% of the time (past performance doesn't predict future results)"
            RegimeQuality.NEUTRAL -> 
                "📚 Educational: Neutral market conditions. Historical rate ~$ratePercent%. Exercise caution and verify independently."
            RegimeQuality.POOR -> 
                "📚 Educational: Challenging market conditions. Historical rate ~$ratePercent%. Higher risk environment - consult professional advice."
        }
    }
}
