package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T44ContinuationStrength : ApexProtocol {
    override val protocolId = "T44"
    override val protocolName = "ContinuationStrength"
    override val weight = 2.6
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["continuationStrong"] = false
            state["continuationStrengthScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ContinuationStrength: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val fusedContinuationScore = state["fusedContinuationScore"] as? Double ?: 0.5
        val smoothedContinuationScore = state["smoothedContinuationScore"] as? Double ?: 0.5
        
        val bothStrong = (fusedContinuationScore > 0.6) && (smoothedContinuationScore > 0.6)
        val continuationStrengthScore = (fusedContinuationScore + smoothedContinuationScore) / 2.0
        
        state["continuationStrong"] = bothStrong
        state["continuationStrengthScore"] = continuationStrengthScore
        
        val passed = bothStrong
        val confidence = continuationStrengthScore
        
        val reason = String.format(
            Locale.US,
            "ContinuationStrength: %.2f (fused=%.2f, smoothed=%.2f) - %s",
            continuationStrengthScore,
            fusedContinuationScore,
            smoothedContinuationScore,
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
