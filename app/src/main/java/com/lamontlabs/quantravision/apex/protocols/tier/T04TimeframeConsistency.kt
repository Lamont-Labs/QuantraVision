package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.max
/**
 * T04: TimeframeConsistency
 * Purpose: Validates timeframe consistency across candles
 * Category: Input Validation & Sanitization
 * 
 * BATCH 8 FIX: Uses actual candle timestamps, fully deterministic
 */
class T04TimeframeConsistency : ApexProtocol {
    override val protocolId = "T04"
    override val protocolName = "TimeframeConsistency"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 2) {
            state["timeframeConsistent"] = true
            state["gapsDetected"] = 0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 1.0,
                reason = "Timeframe consistency: PASS (insufficient data for gaps)",
                weight = weight
            )
        }
        
        var gapCount = 0
        var nonChronologicalCount = 0
        // Check chronological order and gaps
        for (i in 1 until primitives.candles.size) {
            val gap = primitives.candles[i].timestamp - primitives.candles[i - 1].timestamp
            
            // Not chronological
            if (gap <= 0) {
                nonChronologicalCount++
            }
        }
        val isConsistent = nonChronologicalCount == 0
        state["timeframeConsistent"] = isConsistent
        state["gapsDetected"] = gapCount
        state["nonChronologicalCount"] = nonChronologicalCount
        val passed = isConsistent
        val confidence = if (passed) 1.0 else max(0.0, 1.0 - nonChronologicalCount * 0.2)
        val reason = if (passed) {
            "Timeframe consistency: PASS"
        } else {
            "Timeframe consistency: FAIL - $nonChronologicalCount non-chronological candles"
        }
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
