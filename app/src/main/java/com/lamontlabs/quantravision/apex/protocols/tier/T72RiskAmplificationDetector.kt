package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T72RiskAmplificationDetector : ApexProtocol {
    override val protocolId = "T72"
    override val protocolName = "RiskAmplificationDetector"
    override val weight = 3.25
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["riskAmplified"] = true
            state["amplificationScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "RiskAmplificationDetector: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val marketStressLevel = state["marketStressLevel"] as? Double ?: 0.9
        val volatilityExceptionScore = state["volatilityExceptionScore"] as? Double ?: 0.9
        val spikeIntensity = state["spikeIntensity"] as? Double ?: 0.9
        val movementAbnormalityScore = state["movementAbnormalityScore"] as? Double ?: 0.9
        
        val amplificationScore = (marketStressLevel + volatilityExceptionScore +
                                 spikeIntensity + movementAbnormalityScore) / 4.0
        
        val riskAmplified = amplificationScore >= 0.7
        
        state["riskAmplified"] = riskAmplified
        state["amplificationScore"] = amplificationScore
        
        val passed = !riskAmplified
        val confidence = 1.0 - amplificationScore
        
        val reason = String.format(
            Locale.US,
            "RiskAmplificationDetector: %.2f (threshold=0.7) - %s",
            amplificationScore,
            if (passed) "PASS" else "AMPLIFIED"
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
