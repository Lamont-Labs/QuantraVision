package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
/**
 * T15: MultiTimeframeCoherence
 * Purpose: Validates alignment across multiple timeframes (simulated)
 * Category: Momentum & Alignment
 * 
 * BATCH 8 FIX: Safe state access, conservative scoring
 */
class T15MultiTimeframeCoherence : ApexProtocol {
    override val protocolId = "T15"
    override val protocolName = "MultiTimeframeCoherence"
    override val weight = 1.5
    
    override fun execute(primitives: ApexPrimitives, state: MutableMap<String, Any>): ProtocolVerdict {
        if (primitives.candles.isEmpty()) {
            state["mtfCoherent"] = false
            state["mtfCoherenceScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "MultiTimeframeCoherence: No candle data available",
                weight = weight
            )
        }
        
        // Since we don't have multi-timeframe data, use single timeframe metrics
        // Safe state access
        val trendStrength = state["trendStrength"] as? Double ?: 0.5
        val momentumAligned = state["momentumAligned"] as? Boolean ?: true
        val volumeConfirmed = state["volumeConfirmed"] as? Boolean ?: true
        
        // Simulate MTF coherence based on current timeframe quality
        val coherenceScore = when {
            trendStrength > 0.7 && momentumAligned && volumeConfirmed -> 0.95
            trendStrength > 0.5 && momentumAligned -> 0.75
            trendStrength > 0.3 -> 0.55
            else -> 0.35
        }
        val isCoherent = coherenceScore >= 0.6
        state["mtfCoherent"] = isCoherent
        state["mtfCoherenceScore"] = coherenceScore
        val passed = isCoherent
        val confidence = coherenceScore
        val status = if (passed) "coherent" else "divergent"
        val reason = "MTF coherence: ${"%.2f".format(Locale.US, coherenceScore)} - $status"
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
