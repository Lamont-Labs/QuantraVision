package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP20AdaptiveRefinementFinalizer : ApexProtocol {
    override val protocolId = "LP20"
    override val protocolName = "AdaptiveRefinementFinalizer"
    override val weight = 1.50
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val sensitivityAdjusted = state["sensitivityAdjusted"] as? Boolean ?: false
        
        if (!sensitivityAdjusted) {
            state["adaptiveConfidenceModifier"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.2,
                reason = "AdaptiveRefinementFinalizer: Sensitivity not adjusted - FAIL",
                weight = weight
            )
        }
        
        val clampedModifier = state["clampedConfidenceModifier"] as? Double ?: 0.5
        val sensitivity = state["detectionSensitivity"] as? Double ?: 0.5
        
        val finalModifier = (clampedModifier + sensitivity) / 2.0
        
        state["adaptiveConfidenceModifier"] = finalModifier
        state["adaptiveRefinementFinalized"] = true
        
        val passed = true
        val confidence = 0.85
        
        val reason = String.format(
            Locale.US,
            "AdaptiveRefinementFinalizer: Final modifier %.3f written - PASS",
            finalModifier
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
