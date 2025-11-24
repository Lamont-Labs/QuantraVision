package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP16ConfidenceModifierCalculator : ApexProtocol {
    override val protocolId = "LP16"
    override val protocolName = "ConfidenceModifierCalculator"
    override val weight = 1.30
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val suppressionScore = state["suppressionMemoryScore"] as? Double ?: 0.0
        val driftScore = state["driftAdaptationScore"] as? Double ?: 0.0
        val patternScore = state["patternEffectivenessScore"] as? Double ?: 0.0
        
        if (suppressionScore <= 0.0 && driftScore <= 0.0 && patternScore <= 0.0) {
            state["confidenceModifierCalculated"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.2,
                reason = "ConfidenceModifierCalculator: No learning insights available - FAIL",
                weight = weight
            )
        }
        
        val blendedModifier = blendInsights(suppressionScore, driftScore, patternScore)
        
        state["confidenceModifierCalculated"] = true
        state["blendedConfidenceModifier"] = blendedModifier
        
        val passed = blendedModifier > 0.3
        val confidence = blendedModifier
        
        val reason = String.format(
            Locale.US,
            "ConfidenceModifierCalculator: Blended modifier %.3f - %s",
            blendedModifier,
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
    
    private fun blendInsights(suppression: Double, drift: Double, pattern: Double): Double {
        return ((suppression * 0.3) + (drift * 0.3) + (pattern * 0.4)).coerceIn(0.0, 1.0)
    }
}
