package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T77ConfidenceAggregator : ApexProtocol {
    override val protocolId = "T77"
    override val protocolName = "ConfidenceAggregator"
    override val weight = 3.35
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["aggregatedConfidence"] = 0.0
            state["confidenceLevel"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ConfidenceAggregator: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val scores = state.entries
            .filter { it.key.endsWith("Score") && it.value is Double }
            .map { it.value as Double }
        
        val aggregatedConfidence = if (scores.isEmpty()) {
            0.0
        } else {
            scores.average()
        }
        
        val confidenceLevel = aggregatedConfidence
        
        state["aggregatedConfidence"] = aggregatedConfidence
        state["confidenceLevel"] = confidenceLevel
        
        val passed = aggregatedConfidence >= 0.65
        val confidence = aggregatedConfidence
        
        val reason = String.format(
            Locale.US,
            "ConfidenceAggregator: %.2f (from %d scores) - %s",
            aggregatedConfidence,
            scores.size,
            if (passed) "PASS" else "FAIL"
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
