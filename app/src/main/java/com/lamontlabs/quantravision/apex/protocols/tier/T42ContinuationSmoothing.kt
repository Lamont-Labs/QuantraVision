package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T42ContinuationSmoothing : ApexProtocol {
    override val protocolId = "T42"
    override val protocolName = "ContinuationSmoothing"
    override val weight = 2.6
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["smoothedContinuationScore"] = 0.0
            state["continuationSmooth"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ContinuationSmoothing: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val fusedContinuationScore = state["fusedContinuationScore"] as? Double ?: 0.5
        val breakoutCandidate = state["breakoutCandidate"] as? Boolean ?: false
        
        val smoothedContinuationScore = if (breakoutCandidate) {
            (fusedContinuationScore * 0.85 + 0.15).coerceIn(0.0, 1.0)
        } else {
            fusedContinuationScore * 0.95
        }
        
        state["smoothedContinuationScore"] = smoothedContinuationScore
        state["continuationSmooth"] = smoothedContinuationScore >= 0.6
        
        val passed = smoothedContinuationScore >= 0.6
        val confidence = smoothedContinuationScore
        
        val reason = String.format(
            Locale.US,
            "ContinuationSmoothing: %.2f (fused=%.2f, breakout=%s) - %s",
            smoothedContinuationScore,
            fusedContinuationScore,
            breakoutCandidate,
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
