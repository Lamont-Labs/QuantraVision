package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP19SensitivityAdjuster : ApexProtocol {
    override val protocolId = "LP19"
    override val protocolName = "SensitivityAdjuster"
    override val weight = 1.45
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val overrideFlags = state["adaptiveOverrideFlags"] as? Map<*, *> ?: emptyMap<String, Boolean>()
        
        if (overrideFlags.isEmpty()) {
            state["sensitivityAdjusted"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.2,
                reason = "SensitivityAdjuster: Override flags not set - FAIL",
                weight = weight
            )
        }
        
        val patternScore = state["patternEffectivenessScore"] as? Double ?: 0.5
        val driftScore = state["driftAdaptationScore"] as? Double ?: 0.5
        
        val sensitivityAdjustment = adjustSensitivity(patternScore, driftScore)
        
        state["sensitivityAdjusted"] = true
        state["detectionSensitivity"] = sensitivityAdjustment
        
        val passed = sensitivityAdjustment > 0.3
        val confidence = sensitivityAdjustment
        
        val reason = String.format(
            Locale.US,
            "SensitivityAdjuster: Detection sensitivity %.3f - %s",
            sensitivityAdjustment,
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
    
    private fun adjustSensitivity(patternScore: Double, driftScore: Double): Double {
        return ((patternScore + driftScore) / 2.0).coerceIn(0.0, 1.0)
    }
}
