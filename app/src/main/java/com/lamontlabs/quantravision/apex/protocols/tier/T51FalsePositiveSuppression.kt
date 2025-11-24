package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T51FalsePositiveSuppression : ApexProtocol {
    override val protocolId = "T51"
    override val protocolName = "FalsePositiveSuppression"
    override val weight = 2.7
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["suppressionScore"] = 1.0
            state["suppressionActive"] = true
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "FalsePositiveSuppression: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val entropyThresholdOk = state["entropyThresholdOk"] as? Boolean ?: false
        val conflictCount = state["conflictCount"] as? Int ?: 0
        val signalClarityScore = state["signalClarityScore"] as? Double ?: 0.0
        
        val suppressionActive = !entropyThresholdOk || conflictCount > 3 || signalClarityScore < 0.5
        
        val suppressionScore = when {
            !entropyThresholdOk && conflictCount > 3 -> 0.9
            !entropyThresholdOk || conflictCount > 3 -> 0.7
            signalClarityScore < 0.5 -> 0.6
            signalClarityScore < 0.6 -> 0.4
            else -> 0.2
        }
        
        state["suppressionScore"] = suppressionScore
        state["suppressionActive"] = suppressionActive
        
        val passed = !suppressionActive
        val confidence = 1.0 - suppressionScore
        
        val reason = String.format(
            Locale.US,
            "FalsePositiveSuppression: %.2f (entropy=%s, conflicts=%d, clarity=%.2f) - %s",
            suppressionScore,
            entropyThresholdOk,
            conflictCount,
            signalClarityScore,
            if (passed) "PASS" else "SUPPRESSED"
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
