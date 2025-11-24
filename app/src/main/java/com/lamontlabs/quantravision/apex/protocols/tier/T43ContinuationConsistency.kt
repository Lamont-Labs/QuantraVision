package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T43ContinuationConsistency : ApexProtocol {
    override val protocolId = "T43"
    override val protocolName = "ContinuationConsistency"
    override val weight = 2.7
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["continuationConsistent"] = false
            state["consistencyScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ContinuationConsistency: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val trendContinuationOk = state["trendContinuationOk"] as? Boolean ?: false
        val momentumCarry = state["momentumCarry"] as? Double ?: 0.5
        val volumeCarry = state["volumeCarry"] as? Double ?: 0.5
        
        val trendScore = if (trendContinuationOk) 1.0 else 0.0
        val momentumScore = if (momentumCarry > 0.5) 1.0 else 0.0
        val volumeScore = if (volumeCarry > 0.5) 1.0 else 0.0
        
        val agreementCount = trendScore + momentumScore + volumeScore
        val consistencyScore = agreementCount / 3.0
        
        val continuationConsistent = agreementCount >= 3.0
        
        state["continuationConsistent"] = continuationConsistent
        state["consistencyScore"] = consistencyScore
        
        val passed = continuationConsistent
        val confidence = consistencyScore
        
        val reason = String.format(
            Locale.US,
            "ContinuationConsistency: %.2f (trend=%s, momentum=%.2f, volume=%.2f, agreement=%.0f/3) - %s",
            consistencyScore,
            trendContinuationOk,
            momentumCarry,
            volumeCarry,
            agreementCount,
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
