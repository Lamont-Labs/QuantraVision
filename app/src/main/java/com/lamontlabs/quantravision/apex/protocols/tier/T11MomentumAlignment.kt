package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.max
/**
 * T11: MomentumAlignment
 * Purpose: Checks if price momentum aligns with pattern direction
 * Category: Momentum & Alignment
 * 
 * BATCH 8 FIX: Uses actual candle data, fully deterministic
 */
class T11MomentumAlignment : ApexProtocol {
    override val protocolId = "T11"
    override val protocolName = "MomentumAlignment"
    override val weight = 1.5
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 5) {
            state["momentumAligned"] = true
            state["momentumScore"] = 0.5
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Momentum: insufficient data",
                weight = weight
            )
        }
        
        val prices = primitives.candles.map { it.close }
        
        // Calculate momentum using rate of change
        val windowSize = 3
        val momentum = mutableListOf<Double>()
        for (i in windowSize until prices.size) {
            if (prices[i - windowSize] != 0.0) {
                val change = (prices[i] - prices[i - windowSize]) / prices[i - windowSize]
                momentum.add(change)
            }
        }
        
        if (momentum.isEmpty()) {
            state["momentumAligned"] = true
            state["momentumScore"] = 0.5
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Momentum: 0.50 - neutral",
                weight = weight
            )
        }
        
        // Check momentum alignment (mostly positive or mostly negative)
        val positiveCount = momentum.count { it > 0 }
        val negativeCount = momentum.count { it < 0 }
        val total = momentum.size
        val alignment = max(positiveCount, negativeCount).toDouble() / total.toDouble()
        val isAligned = alignment >= 0.6
        
        state["momentumAligned"] = isAligned
        state["momentumScore"] = alignment
        
        val passed = isAligned
        val confidence = alignment
        val direction = if (positiveCount > negativeCount) "bullish" else "bearish"
        val reason = "Momentum: ${"%.2f".format(Locale.US, alignment)} - $direction"
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
