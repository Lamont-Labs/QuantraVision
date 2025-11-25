package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
/**
 * T09: SupportResistanceDetection
 * Purpose: Identifies key support/resistance levels
 * Category: Structural Quality
 * 
 * BATCH 8 FIX: Uses actual candle data, fully deterministic
 */
class T09SupportResistanceDetection : ApexProtocol {
    override val protocolId = "T09"
    override val protocolName = "SupportResistanceDetection"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 5) {
            state["keyLevels"] = emptyList<Double>()
            state["supportResistanceCount"] = 0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Key levels: insufficient data",
                weight = weight
            )
        }
        
        val prices = primitives.candles.map { it.close }
        val keyLevels = mutableListOf<Double>()
        val priceRange = (prices.maxOrNull() ?: 0.0) - (prices.minOrNull() ?: 0.0)
        val threshold = priceRange * 0.02
        // Find local extrema (peaks and troughs)
        for (i in 2 until prices.size - 2) {
            val isPeak = prices[i] > prices[i - 1] && prices[i] > prices[i + 1] &&
                         prices[i] > prices[i - 2] && prices[i] > prices[i + 2]
            val isTrough = prices[i] < prices[i - 1] && prices[i] < prices[i + 1] &&
                           prices[i] < prices[i - 2] && prices[i] < prices[i + 2]
            
            if (isPeak || isTrough) {
                val isDistinct = keyLevels.all { abs(it - prices[i]) > threshold }
                if (isDistinct) {
                    keyLevels.add(prices[i])
                }
            }
        }
        state["keyLevels"] = keyLevels.take(10)
        state["supportResistanceCount"] = keyLevels.size
        val passed = keyLevels.isNotEmpty()
        val confidence = if (keyLevels.isNotEmpty()) {
            max(0.5, min(1.0, keyLevels.size / 5.0))
        } else {
            0.3
        }
        val reason = "Key levels: ${keyLevels.size} identified"
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
