package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.abs
/**
 * T18: RegimeValidation
 * Purpose: Validates market regime matches pattern requirements
 * Category: Entropy & Conflict Detection
 * 
 * BATCH 8 FIX: Uses actual candle data, NO hashCode(), fully deterministic
 */
class T18RegimeValidation : ApexProtocol {
    override val protocolId = "T18"
    override val protocolName = "RegimeValidation"
    override val weight = 1.5
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 10) {
            state["currentRegime"] = "unknown"
            state["expectedRegime"] = "any"
            state["regimeMatch"] = true
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.6,
                reason = "Regime: unknown vs any - MATCH (insufficient data)",
                weight = weight
            )
        }
        
        // Detect current regime from actual price data
        val prices = primitives.candles.map { it.close }
        val currentRegime = detectRegime(prices)
        // Determine expected regime based on state
        val expectedRegime = determineExpectedRegime(state)
        // Check regime match
        val regimeMatch = isRegimeMatch(currentRegime, expectedRegime)
        state["currentRegime"] = currentRegime
        state["expectedRegime"] = expectedRegime
        state["regimeMatch"] = regimeMatch
        val passed = regimeMatch
        val confidence = if (passed) 0.85 else 0.35
        val matchStatus = if (regimeMatch) "MATCH" else "MISMATCH"
        val reason = "Regime: $currentRegime vs $expectedRegime - $matchStatus"
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = passed,
            confidence = confidence,
            reason = reason,
            weight = weight
        )
    }
    private fun detectRegime(prices: List<Double>): String {
        if (prices.size < 10) return "unknown"
        // Calculate trend
        val first = prices.take(prices.size / 3).average()
        val last = prices.takeLast(prices.size / 3).average()
        val trend = (last - first) / first
        // Calculate volatility
        val returns = prices.zipWithNext { a, b -> (b - a) / a }
        val stdDev = calculateStdDev(returns)
        // Classify regime
        return when {
            abs(trend) < 0.01 && stdDev < 0.01 -> "ranging-low-vol"
            abs(trend) < 0.01 && stdDev >= 0.01 -> "ranging-high-vol"
            trend > 0.02 && stdDev < 0.02 -> "trending-up-stable"
            trend > 0.02 && stdDev >= 0.02 -> "trending-up-volatile"
            trend < -0.02 && stdDev < 0.02 -> "trending-down-stable"
            trend < -0.02 && stdDev >= 0.02 -> "trending-down-volatile"
            else -> "transitional"
        }
    }
    private fun determineExpectedRegime(state: Map<String, Any>): String {
        // Safe state access - use state information to infer expected regime
        val trendStrength = state["trendStrength"] as? Double ?: 0.5
        val volatilityState = state["volatility"] as? String ?: "normal"
        return when {
            trendStrength > 0.6 && volatilityState in listOf("low", "normal") -> "trending-up-stable"
            trendStrength > 0.6 && volatilityState in listOf("high", "extreme") -> "trending-up-volatile"
            trendStrength < 0.4 && volatilityState in listOf("low", "normal") -> "ranging-low-vol"
            trendStrength < 0.4 && volatilityState in listOf("high", "extreme") -> "ranging-high-vol"
            else -> "any"
        }
    }
    private fun isRegimeMatch(current: String, expected: String): Boolean {
        // Exact match
        if (current == expected) return true
        // "any" matches everything
        if (expected == "any") return true
        // Compatible regimes
        val compatibleRegimes = mapOf(
            "trending-up-stable" to listOf("trending-up-volatile", "transitional"),
            "trending-down-stable" to listOf("trending-down-volatile", "transitional"),
            "ranging-low-vol" to listOf("transitional"),
            "ranging-high-vol" to listOf("transitional"),
            "transitional" to listOf("ranging-low-vol", "ranging-high-vol")
        )
        return compatibleRegimes[current]?.contains(expected) == true ||
               compatibleRegimes[expected]?.contains(current) == true
    }
    private fun calculateStdDev(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
}
