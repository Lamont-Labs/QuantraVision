package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP14TierWeightedAdjuster : ApexProtocol {
    override val protocolId = "LP14"
    override val protocolName = "TierWeightedAdjuster"
    override val weight = 1.35
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val bandMapped = state["reliabilityBandMapped"] as? Boolean ?: false
        
        if (!bandMapped) {
            state["tierWeightedAdjustment"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.2,
                reason = "TierWeightedAdjuster: Reliability band not mapped - FAIL",
                weight = weight
            )
        }
        
        val effectivenessScore = state["patternEffectivenessScore"] as? Double ?: 0.0
        val adjustment = applyTierWeightedAdjustment(effectivenessScore, context)
        
        state["tierWeightedAdjustment"] = adjustment
        
        val passed = adjustment > 0.3
        val confidence = adjustment
        
        val reason = String.format(
            Locale.US,
            "TierWeightedAdjuster: Tier adjustment %.3f - %s",
            adjustment,
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
    
    private fun applyTierWeightedAdjustment(score: Double, context: ApexScanContext): Double {
        val tierMultiplier = when (context.tier.uppercase()) {
            "APEX", "APEX_ULTRA", "ULTRA" -> 1.2
            "PRO", "STANDARD" -> 1.0
            "BASIC", "STARTER" -> 0.9
            else -> 0.8
        }
        return (score * tierMultiplier).coerceIn(0.0, 1.0)
    }
}
