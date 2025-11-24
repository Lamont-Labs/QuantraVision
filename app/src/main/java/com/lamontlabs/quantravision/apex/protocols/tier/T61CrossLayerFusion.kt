package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T61CrossLayerFusion : ApexProtocol {
    override val protocolId = "T61"
    override val protocolName = "CrossLayerFusion"
    override val weight = 3.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["crossLayerScore"] = 0.0
            state["layersFused"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "CrossLayerFusion: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val finalContinuationScore = state["finalContinuationScore"] as? Double ?: 0.0
        val regimeCoherenceScore = state["regimeCoherenceScore"] as? Double ?: 0.5
        val finalSuppressionScore = state["finalSuppressionScore"] as? Double ?: 0.8
        val marketStressLevel = state["marketStressLevel"] as? Double ?: 0.8
        
        val crossLayerScore = 0.3 * finalContinuationScore +
                             0.25 * regimeCoherenceScore +
                             0.25 * (1.0 - finalSuppressionScore) +
                             0.2 * (1.0 - marketStressLevel)
        
        val layersFused = crossLayerScore >= 0.65
        
        state["crossLayerScore"] = crossLayerScore
        state["layersFused"] = layersFused
        
        val passed = crossLayerScore >= 0.65
        val confidence = crossLayerScore
        
        val reason = String.format(
            Locale.US,
            "CrossLayerFusion: %.2f (cont=%.2f, regime=%.2f, supp=%.2f, stress=%.2f) - %s",
            crossLayerScore,
            finalContinuationScore,
            regimeCoherenceScore,
            finalSuppressionScore,
            marketStressLevel,
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
