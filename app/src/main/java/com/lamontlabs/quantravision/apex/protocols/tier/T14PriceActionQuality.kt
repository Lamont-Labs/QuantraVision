package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
/**
 * T14: PriceActionQuality
 * Purpose: Validates price action cleanliness (no whipsaws, false breaks)
 * Category: Momentum & Alignment
 * 
 * BATCH 8 FIX: Uses actual candle data, fully deterministic
 */
class T14PriceActionQuality : ApexProtocol {
    override val protocolId = "T14"
    override val protocolName = "PriceActionQuality"
    override val weight = 1.5
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 3) {
            state["priceActionQuality"] = 0.5
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Price action: insufficient data",
                weight = weight
            )
        }
        
        var qualityScore = 1.0
        // Calculate candle body ratio (body vs wick)
        val bodyRatios = primitives.candles.map { candle ->
            val bodySize = abs(candle.close - candle.open)
            val totalRange = candle.high - candle.low
            if (totalRange > 0) bodySize / totalRange else 0.0
        }
        val avgBodyRatio = bodyRatios.average()
        // Penalize for too many doji/spinning tops (small bodies)
        val dojiCount = bodyRatios.count { it < 0.2 }
        val dojiRatio = dojiCount.toDouble() / primitives.candles.size.toDouble()
        qualityScore -= dojiRatio * 0.3
        // Check for directional consistency (no excessive whipsaws)
        val prices = primitives.candles.map { it.close }
        var directionChanges = 0
        for (i in 2 until prices.size) {
            val prev = prices[i - 1] - prices[i - 2]
            val curr = prices[i] - prices[i - 1]
            if ((prev > 0 && curr < 0) || (prev < 0 && curr > 0)) {
                directionChanges++
            }
        }
        val changeRatio = directionChanges.toDouble() / (prices.size - 2).toDouble()
        qualityScore -= changeRatio * 0.4
        qualityScore = max(0.0, min(1.0, qualityScore))
        state["priceActionQuality"] = qualityScore
        val passed = qualityScore >= 0.5
        val confidence = qualityScore
        val quality = when {
            qualityScore >= 0.8 -> "clean"
            qualityScore >= 0.6 -> "acceptable"
            qualityScore >= 0.4 -> "choppy"
            else -> "poor"
        }
        val reason = "Price action quality: ${"%.2f".format(Locale.US, qualityScore)} - $quality"
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
