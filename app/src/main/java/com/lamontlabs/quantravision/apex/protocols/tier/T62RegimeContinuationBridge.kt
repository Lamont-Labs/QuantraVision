package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T62RegimeContinuationBridge : ApexProtocol {
    override val protocolId = "T62"
    override val protocolName = "RegimeContinuationBridge"
    override val weight = 3.05
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["regimeContinuationAligned"] = false
            state["bridgeScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "RegimeContinuationBridge: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val continuationValidated = state["continuationValidated"] as? Boolean ?: false
        val regimeAlignmentOk = state["regimeAlignmentOk"] as? Boolean ?: false
        val crossRegimeCoherent = state["crossRegimeCoherent"] as? Boolean ?: false
        val volatilityGuardOk = state["volatilityGuardOk"] as? Boolean ?: false
        
        val alignedCount = listOf(
            continuationValidated,
            regimeAlignmentOk,
            crossRegimeCoherent,
            volatilityGuardOk
        ).count { it }
        
        val bridgeScore = alignedCount / 4.0
        val regimeContinuationAligned = bridgeScore >= 0.75
        
        state["regimeContinuationAligned"] = regimeContinuationAligned
        state["bridgeScore"] = bridgeScore
        
        val passed = bridgeScore >= 0.75
        val confidence = bridgeScore
        
        val reason = String.format(
            Locale.US,
            "RegimeContinuationBridge: %.2f (%d/4 aligned) - %s",
            bridgeScore,
            alignedCount,
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
