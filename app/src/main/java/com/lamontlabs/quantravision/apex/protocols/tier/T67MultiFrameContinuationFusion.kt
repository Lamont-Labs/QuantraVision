package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T67MultiFrameContinuationFusion : ApexProtocol {
    override val protocolId = "T67"
    override val protocolName = "MultiFrameContinuationFusion"
    override val weight = 3.15
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 35) {
            state["multiFrameContinuationScore"] = 0.0
            state["multiFrameFused"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "MultiFrameContinuationFusion: Insufficient candles (need >=35, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val finalContinuationScore = state["finalContinuationScore"] as? Double ?: 0.0
        val consistencyScore = state["consistencyScore"] as? Double ?: 0.0
        val continuationStrengthScore = state["continuationStrengthScore"] as? Double ?: 0.0
        val driftScore = state["driftScore"] as? Double ?: 0.0
        
        val base = 0.4 * finalContinuationScore +
                   0.3 * consistencyScore +
                   0.3 * continuationStrengthScore
        
        val multiFrameContinuationScore = base * (1.0 - 0.2 * driftScore)
        val multiFrameFused = multiFrameContinuationScore >= 0.7
        
        state["multiFrameContinuationScore"] = multiFrameContinuationScore
        state["multiFrameFused"] = multiFrameFused
        
        val passed = multiFrameContinuationScore >= 0.7
        val confidence = multiFrameContinuationScore
        
        val reason = String.format(
            Locale.US,
            "MultiFrameContinuationFusion: %.2f (base=%.2f, drift=%.2f) - %s",
            multiFrameContinuationScore,
            base,
            driftScore,
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
