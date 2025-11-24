package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP18OverrideFlagSetter : ApexProtocol {
    override val protocolId = "LP18"
    override val protocolName = "OverrideFlagSetter"
    override val weight = 1.40
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val clampEnforced = state["clampEnforced"] as? Boolean ?: false
        
        if (!clampEnforced) {
            state["adaptiveOverrideFlags"] = emptyMap<String, Boolean>()
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.2,
                reason = "OverrideFlagSetter: Clamp not enforced - FAIL",
                weight = weight
            )
        }
        
        val clampedModifier = state["clampedConfidenceModifier"] as? Double ?: 0.5
        val suppressionScore = state["suppressionMemoryScore"] as? Double ?: 0.0
        
        val overrideFlags = setOverrideFlags(clampedModifier, suppressionScore)
        
        state["adaptiveOverrideFlags"] = overrideFlags
        
        val passed = true
        val confidence = 0.8
        
        val reason = String.format(
            Locale.US,
            "OverrideFlagSetter: Override flags set (clamp: %s, suppression: %s) - PASS",
            overrideFlags["confidenceClampActive"],
            overrideFlags["learningSuppressionSuggested"]
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
    
    private fun setOverrideFlags(modifier: Double, suppressionScore: Double): Map<String, Boolean> {
        return mapOf(
            "confidenceClampActive" to (modifier < 0.15 || modifier > 0.9),
            "learningSuppressionSuggested" to (suppressionScore > 0.7)
        )
    }
}
