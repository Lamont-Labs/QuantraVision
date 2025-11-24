package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP25LearningStateFinalizer : ApexProtocol {
    override val protocolId = "LP25"
    override val protocolName = "LearningStateFinalizer"
    override val weight = 1.60
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        // FAIL-CLOSED: Check candles
        if (primitives.candles.size < 10) {
            resetLearningState(state)
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "LearningStateFinalizer: Insufficient candles - FAIL (fail-closed)",
                weight = weight
            )
        }
        
        // FAIL-CLOSED: Verify critical finalization artifacts exist
        val requiredMarkers = listOf(
            "suppressionMemoryScore",
            "driftAdaptationScore",
            "patternEffectivenessScore",
            "adaptiveConfidenceModifier",
            "learningProofDigest"
        )
        
        val missingMarkers = requiredMarkers.filter { !state.containsKey(it) }
        
        if (missingMarkers.isNotEmpty()) {
            resetLearningState(state)
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = String.format(
                    Locale.US,
                    "LearningStateFinalizer: Missing critical markers (%s) - FAIL (fail-closed)",
                    missingMarkers.joinToString(", ")
                ),
                weight = weight
            )
        }
        
        // Verify proof digest is valid (not placeholder)
        val learningProofDigest = state["learningProofDigest"] as? String ?: ""
        
        if (learningProofDigest.isEmpty() || learningProofDigest == "unknown" || learningProofDigest == "digest_error") {
            resetLearningState(state)
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "LearningStateFinalizer: Invalid proof digest ($learningProofDigest) - FAIL (fail-closed)",
                weight = weight
            )
        }
        
        state["learningStateReady"] = true
        
        val passed = true
        val confidence = 0.95
        
        val reason = String.format(
            Locale.US,
            "LearningStateFinalizer: Learning state ready (digest: %s) - PASS",
            learningProofDigest.take(16)
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
    
    private fun resetLearningState(state: MutableMap<String, Any>) {
        state["suppressionMemoryScore"] = 0.0
        state["driftAdaptationScore"] = 0.0
        state["patternEffectivenessScore"] = 0.0
        state["adaptiveConfidenceModifier"] = 0.0
        state["learningProofDigest"] = ""
    }
}
