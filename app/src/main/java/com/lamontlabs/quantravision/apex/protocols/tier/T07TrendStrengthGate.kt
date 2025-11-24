package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.max
import kotlin.math.min
/**
 * T07: TrendStrengthGate
 * Purpose: Validates trend strength using moving averages or slope
 * Category: Structural Quality
 * 
 * BATCH 8 FIX: Uses actual candle data, fully deterministic
 */
class T07TrendStrengthGate : ApexProtocol {
    override val protocolId = "T07"
    override val protocolName = "TrendStrengthGate"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 3) {
            state["trendStrength"] = 0.5
            state["trendDirection"] = "unknown"
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Trend strength: insufficient data",
                weight = weight
            )
        }
        
        val prices = primitives.candles.map { it.close }
        // Simple linear regression
        val n = prices.size
        val x = (0 until n).map { it.toDouble() }
        val y = prices
        val xMean = x.average()
        val yMean = y.average()
        var numerator = 0.0
        var denominator = 0.0
        for (i in x.indices) {
            numerator += (x[i] - xMean) * (y[i] - yMean)
            denominator += (x[i] - xMean) * (x[i] - xMean)
        }
        
        val slope = if (denominator != 0.0) numerator / denominator else 0.0
        
        // Calculate R-squared for consistency
        val yPredicted = x.map { slope * (it - xMean) + yMean }
        val ssTotal = y.map { (it - yMean) * (it - yMean) }.sum()
        val ssResidual = y.zip(yPredicted).map { (actual, predicted) ->
            (actual - predicted) * (actual - predicted)
        }.sum()
        val rSquared = if (ssTotal > 0) 1.0 - (ssResidual / ssTotal) else 0.0
        val trendStrength = min(1.0, max(0.0, rSquared))
        
        // Determine direction
        val direction = when {
            slope > 0.5 -> "uptrend"
            slope < -0.5 -> "downtrend"
            else -> "sideways"
        }
        
        state["trendStrength"] = trendStrength
        state["trendDirection"] = direction
        val passed = trendStrength >= 0.3
        val confidence = trendStrength
        val reason = "Trend strength: ${"%.2f".format(Locale.US, trendStrength)} - $direction"
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = passed,
            confidence = confidence,
            reason = reason,
            weight = weight
        )
    }
}
