package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T65CoherenceValidator : ApexProtocol {
    override val protocolId = "T65"
    override val protocolName = "CoherenceValidator"
    override val weight = 3.2
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["coherenceValidated"] = false
            state["coherenceScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "CoherenceValidator: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val continuationConsistent = state["continuationConsistent"] as? Boolean ?: false
        val crossRegimeCoherent = state["crossRegimeCoherent"] as? Boolean ?: false
        val signalClarity = state["signalClarity"] as? Double ?: 0.0
        val conflictCount = state["conflictCount"] as? Int ?: 5
        
        val coherenceScore = ((if (continuationConsistent) 1.0 else 0.0) +
                             (if (crossRegimeCoherent) 1.0 else 0.0) +
                             signalClarity) / 3.0
        
        val coherenceValidated = coherenceScore >= 0.7 && conflictCount <= 3
        
        state["coherenceValidated"] = coherenceValidated
        state["coherenceScore"] = coherenceScore
        
        val passed = coherenceValidated
        val confidence = if (coherenceValidated) coherenceScore else 0.0
        
        val reason = String.format(
            Locale.US,
            "CoherenceValidator: %.2f (conflicts=%d) - %s",
            coherenceScore,
            conflictCount,
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
