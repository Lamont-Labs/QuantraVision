package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T27TrendContinuation : ApexProtocol {
    override val protocolId = "T27"
    override val protocolName = "TrendContinuation"
    override val weight = 1.8
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 15) {
            state["trendContinuationOk"] = false
            state["trendCarry"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "TrendContinuation: Insufficient candles (need >=15, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val trendStrength = state["trendStrength"] as? Double ?: 0.5
        val trendDirection = state["trendDirection"] as? String ?: "UNKNOWN"
        
        val last5 = primitives.candles.takeLast(5)
        val recentSlope = if (last5.size >= 2) {
            (last5.last().close - last5.first().close) / last5.first().close
        } else 0.0
        
        val allCandles = primitives.candles
        val overallSlope = if (allCandles.size >= 2) {
            (allCandles.last().close - allCandles.first().close) / allCandles.first().close
        } else 0.0
        
        val sameDirection = when {
            recentSlope > 0.005 && overallSlope > 0.005 -> true
            recentSlope < -0.005 && overallSlope < -0.005 -> true
            else -> false
        }
        
        val trendCarry = if (sameDirection) trendStrength * 0.9 else trendStrength * 0.3
        
        state["trendContinuationOk"] = sameDirection && trendStrength >= 0.5
        state["trendCarry"] = trendCarry
        
        val passed = sameDirection && trendStrength >= 0.5
        val confidence = if (passed) trendStrength else trendStrength * 0.5
        
        val reason = String.format(
            Locale.US,
            "TrendContinuation: %s - %s (strength=%.2f, carry=%.2f, direction=%s)",
            if (sameDirection) "ALIGNED" else "DIVERGED",
            if (passed) "PASS" else "FAIL",
            trendStrength,
            trendCarry,
            trendDirection
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
