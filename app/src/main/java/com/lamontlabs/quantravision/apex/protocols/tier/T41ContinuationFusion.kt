package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T41ContinuationFusion : ApexProtocol {
    override val protocolId = "T41"
    override val protocolName = "ContinuationFusion"
    override val weight = 2.5
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["fusedContinuationScore"] = 0.0
            state["continuationFused"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ContinuationFusion: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val continuationScore = state["continuationScore"] as? Double ?: 0.5
        val trendContinuationOk = state["trendContinuationOk"] as? Boolean ?: false
        val momentumCarry = state["momentumCarry"] as? Double ?: 0.5
        val volumeCarry = state["volumeCarry"] as? Double ?: 0.5
        
        val trendScore = if (trendContinuationOk) 0.8 else 0.3
        
        val fusedContinuationScore = (0.3 * trendScore) + 
                                     (0.3 * momentumCarry) + 
                                     (0.25 * volumeCarry) + 
                                     (0.15 * continuationScore)
        
        state["fusedContinuationScore"] = fusedContinuationScore
        state["continuationFused"] = fusedContinuationScore >= 0.65
        
        val passed = fusedContinuationScore >= 0.65
        val confidence = fusedContinuationScore
        
        val reason = String.format(
            Locale.US,
            "ContinuationFusion: %.2f (trend=%.2f, momentum=%.2f, volume=%.2f, pattern=%.2f) - %s",
            fusedContinuationScore,
            trendScore,
            momentumCarry,
            volumeCarry,
            continuationScore,
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
