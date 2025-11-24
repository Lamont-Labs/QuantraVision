package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T55AdaptiveSuppressionThreshold : ApexProtocol {
    override val protocolId = "T55"
    override val protocolName = "AdaptiveSuppressionThreshold"
    override val weight = 2.9
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["adaptiveSuppressionActive"] = true
            state["finalSuppressionScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "AdaptiveSuppressionThreshold: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val suppressionActive = state["suppressionActive"] as? Boolean ?: false
        val patternSuppressed = state["patternSuppressed"] as? Boolean ?: false
        val noiseSuppressed = state["noiseSuppressed"] as? Boolean ?: false
        val conflictSuppressed = state["conflictSuppressed"] as? Boolean ?: false
        
        val suppressionScore = state["suppressionScore"] as? Double ?: 0.5
        val patternSuppressionScore = state["patternSuppressionScore"] as? Double ?: 0.5
        val noiseSuppressionLevel = state["noiseSuppressionLevel"] as? Double ?: 0.5
        val conflictSuppressionScore = state["conflictSuppressionScore"] as? Double ?: 0.5
        
        val adaptiveThreshold = state["adaptiveThreshold"] as? Double ?: 0.65
        
        val suppressionCount = listOf(
            suppressionActive,
            patternSuppressed,
            noiseSuppressed,
            conflictSuppressed
        ).count { it }
        
        val finalSuppressionScore = (suppressionScore + patternSuppressionScore + 
                                     noiseSuppressionLevel + conflictSuppressionScore) / 4.0
        
        val adaptiveSuppressionActive = suppressionCount >= 2 || finalSuppressionScore > adaptiveThreshold
        
        state["adaptiveSuppressionActive"] = adaptiveSuppressionActive
        state["finalSuppressionScore"] = finalSuppressionScore
        
        val passed = !adaptiveSuppressionActive
        val confidence = 1.0 - finalSuppressionScore
        
        val reason = String.format(
            Locale.US,
            "AdaptiveSuppressionThreshold: %.2f (count=%d/4, threshold=%.2f) - %s",
            finalSuppressionScore,
            suppressionCount,
            adaptiveThreshold,
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
