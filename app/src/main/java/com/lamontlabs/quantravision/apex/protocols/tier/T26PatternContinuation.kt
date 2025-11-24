package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T26PatternContinuation : ApexProtocol {
    override val protocolId = "T26"
    override val protocolName = "PatternContinuation"
    override val weight = 1.7
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["continuationScore"] = 0.0
            state["patternContinuing"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "PatternContinuation: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val structureComplete = state["structureComplete"] as? Boolean ?: false
        val trendDirection = state["trendDirection"] as? String ?: "UNKNOWN"
        
        val last10 = primitives.candles.takeLast(10)
        val prevCandles = primitives.candles.dropLast(10).takeLast(10)
        
        val last10Trend = if (last10.isNotEmpty()) {
            val firstPrice = last10.first().close
            val lastPrice = last10.last().close
            when {
                lastPrice > firstPrice * 1.01 -> "UP"
                lastPrice < firstPrice * 0.99 -> "DOWN"
                else -> "SIDEWAYS"
            }
        } else "UNKNOWN"
        
        val prevTrend = if (prevCandles.isNotEmpty()) {
            val firstPrice = prevCandles.first().close
            val lastPrice = prevCandles.last().close
            when {
                lastPrice > firstPrice * 1.01 -> "UP"
                lastPrice < firstPrice * 0.99 -> "DOWN"
                else -> "SIDEWAYS"
            }
        } else "UNKNOWN"
        
        val trendConsistent = (last10Trend == prevTrend) && (last10Trend == trendDirection || trendDirection == "UNKNOWN")
        val continuationScore = if (structureComplete && trendConsistent) 0.8 else if (trendConsistent) 0.6 else 0.3
        
        state["continuationScore"] = continuationScore
        state["patternContinuing"] = continuationScore >= 0.6
        
        val passed = continuationScore >= 0.6
        val confidence = continuationScore
        
        val reason = String.format(
            Locale.US,
            "PatternContinuation: %.2f - %s (structure=%s, direction=%s, consistent=%s)",
            continuationScore,
            if (passed) "PASS" else "FAIL",
            structureComplete,
            trendDirection,
            trendConsistent
        )
        
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
