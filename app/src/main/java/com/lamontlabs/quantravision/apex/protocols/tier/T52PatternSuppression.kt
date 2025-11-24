package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T52PatternSuppression : ApexProtocol {
    override val protocolId = "T52"
    override val protocolName = "PatternSuppression"
    override val weight = 2.8
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["patternSuppressed"] = true
            state["patternSuppressionScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "PatternSuppression: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val continuationValidated = state["continuationValidated"] as? Boolean ?: false
        val structureComplete = state["structureComplete"] as? Boolean ?: false
        
        val shouldSuppress = !continuationValidated || !structureComplete
        
        val patternSuppressionScore = when {
            !structureComplete && !continuationValidated -> 0.95
            !structureComplete -> 0.85
            !continuationValidated -> 0.80
            else -> 0.2
        }
        
        state["patternSuppressed"] = shouldSuppress
        state["patternSuppressionScore"] = patternSuppressionScore
        
        val passed = !shouldSuppress
        val confidence = if (shouldSuppress) 0.0 else 0.8
        
        val reason = String.format(
            Locale.US,
            "PatternSuppression: %.2f (continuation=%s, structure=%s) - %s",
            patternSuppressionScore,
            continuationValidated,
            structureComplete,
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
