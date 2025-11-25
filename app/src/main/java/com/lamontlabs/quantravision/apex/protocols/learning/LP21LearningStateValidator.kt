package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP21LearningStateValidator : ApexProtocol {
    override val protocolId = "LP21"
    override val protocolName = "LearningStateValidator"
    override val weight = 1.40
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val requiredMarkers = listOf(
            "suppressionMemoryScore",
            "driftAdaptationScore",
            "patternEffectivenessScore",
            "adaptiveConfidenceModifier"
        )
        
        val missingMarkers = requiredMarkers.filter { !state.containsKey(it) }
        
        if (missingMarkers.isNotEmpty()) {
            state["learningStateValid"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = String.format(
                    Locale.US,
                    "LearningStateValidator: Missing markers [%s] - FAIL",
                    missingMarkers.joinToString(", ")
                ),
                weight = weight
            )
        }
        
        state["learningStateValid"] = true
        
        val passed = true
        val confidence = 0.9
        
        val reason = String.format(
            Locale.US,
            "LearningStateValidator: All %d required markers present - PASS",
            requiredMarkers.size
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
