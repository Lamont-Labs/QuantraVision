package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP13ContextualReliabilityMapper : ApexProtocol {
    override val protocolId = "LP13"
    override val protocolName = "ContextualReliabilityMapper"
    override val weight = 1.30
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val effectivenessScore = state["patternEffectivenessScore"] as? Double ?: 0.0
        
        if (effectivenessScore <= 0.0) {
            state["reliabilityBandMapped"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.2,
                reason = "ContextualReliabilityMapper: Invalid effectiveness score - FAIL",
                weight = weight
            )
        }
        
        val reliabilityBand = mapToReliabilityBand(effectivenessScore, context)
        
        state["reliabilityBandMapped"] = true
        state["contextualReliabilityBand"] = reliabilityBand
        
        val passed = reliabilityBand != "LOW"
        val confidence = when (reliabilityBand) {
            "HIGH" -> 0.85
            "MEDIUM" -> 0.65
            else -> 0.35
        }
        
        val reason = String.format(
            Locale.US,
            "ContextualReliabilityMapper: Reliability band %s - %s",
            reliabilityBand,
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
    
    private fun mapToReliabilityBand(score: Double, context: ApexScanContext): String {
        return when {
            score >= 0.7 -> "HIGH"
            score >= 0.4 -> "MEDIUM"
            else -> "LOW"
        }
    }
}
