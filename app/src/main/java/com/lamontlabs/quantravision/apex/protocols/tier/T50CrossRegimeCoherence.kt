package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T50CrossRegimeCoherence : ApexProtocol {
    override val protocolId = "T50"
    override val protocolName = "CrossRegimeCoherence"
    override val weight = 2.8
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["crossRegimeCoherent"] = false
            state["regimeCoherenceScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "CrossRegimeCoherence: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val regimeAlignmentOk = state["regimeAlignmentOk"] as? Boolean ?: false
        val sectorCompatible = state["sectorCompatible"] as? Boolean ?: false
        val volatilityRegimeMatch = state["volatilityRegimeMatch"] as? Boolean ?: false
        val regimeStable = state["regimeStable"] as? Boolean ?: false
        val mtfCoherenceScore = state["mtfCoherenceScore"] as? Double ?: 0.5
        
        val regimeAlignmentScore = state["regimeAlignmentScore"] as? Double ?: 0.5
        val sectorCompatibilityScore = state["sectorCompatibilityScore"] as? Double ?: 0.5
        val volatilityRegimeScore = state["volatilityRegimeScore"] as? Double ?: 0.5
        val regimeStabilityScore = state["regimeStabilityScore"] as? Double ?: 0.5
        
        val passCount = listOf(
            regimeAlignmentOk, 
            sectorCompatible, 
            volatilityRegimeMatch, 
            regimeStable
        ).count { it }
        
        val crossRegimeCoherent = passCount >= 3
        
        val regimeCoherenceScore = (regimeAlignmentScore + sectorCompatibilityScore + 
                                    volatilityRegimeScore + regimeStabilityScore + 
                                    mtfCoherenceScore) / 5.0
        
        state["crossRegimeCoherent"] = crossRegimeCoherent
        state["regimeCoherenceScore"] = regimeCoherenceScore
        
        val passed = crossRegimeCoherent
        val confidence = regimeCoherenceScore
        
        val reason = String.format(
            Locale.US,
            "CrossRegimeCoherence: %.2f (passed=%d/4, mtf=%.2f) - %s",
            regimeCoherenceScore,
            passCount,
            mtfCoherenceScore,
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
