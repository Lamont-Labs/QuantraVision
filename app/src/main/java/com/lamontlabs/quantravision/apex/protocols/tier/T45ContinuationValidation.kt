package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T45ContinuationValidation : ApexProtocol {
    override val protocolId = "T45"
    override val protocolName = "ContinuationValidation"
    override val weight = 2.7
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["continuationValidated"] = false
            state["finalContinuationScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ContinuationValidation: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val continuationFused = state["continuationFused"] as? Boolean ?: false
        val continuationSmooth = state["continuationSmooth"] as? Boolean ?: false
        val continuationConsistent = state["continuationConsistent"] as? Boolean ?: false
        val continuationStrong = state["continuationStrong"] as? Boolean ?: false
        
        val fusedContinuationScore = state["fusedContinuationScore"] as? Double ?: 0.5
        val smoothedContinuationScore = state["smoothedContinuationScore"] as? Double ?: 0.5
        val consistencyScore = state["consistencyScore"] as? Double ?: 0.5
        val continuationStrengthScore = state["continuationStrengthScore"] as? Double ?: 0.5
        
        val passCount = listOf(continuationFused, continuationSmooth, continuationConsistent, continuationStrong).count { it }
        val continuationValidated = passCount >= 3
        
        val finalContinuationScore = (fusedContinuationScore + smoothedContinuationScore + 
                                      consistencyScore + continuationStrengthScore) / 4.0
        
        state["continuationValidated"] = continuationValidated
        state["finalContinuationScore"] = finalContinuationScore
        
        val passed = continuationValidated
        val confidence = finalContinuationScore
        
        val reason = String.format(
            Locale.US,
            "ContinuationValidation: %.2f (passed=%d/4) - %s",
            finalContinuationScore,
            passCount,
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
