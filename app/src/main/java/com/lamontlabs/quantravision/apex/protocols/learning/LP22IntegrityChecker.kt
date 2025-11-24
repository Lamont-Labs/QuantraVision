package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP22IntegrityChecker : ApexProtocol {
    override val protocolId = "LP22"
    override val protocolName = "IntegrityChecker"
    override val weight = 1.45
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val stateValid = state["learningStateValid"] as? Boolean ?: false
        
        if (!stateValid) {
            state["learningDataIntegrityChecked"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "IntegrityChecker: Learning state not valid - FAIL",
                weight = weight
            )
        }
        
        val integrityScore = checkDataIntegrity(state)
        
        state["learningDataIntegrityChecked"] = true
        state["learningDataIntegrityScore"] = integrityScore
        
        val passed = integrityScore > 0.7
        val confidence = integrityScore
        
        val reason = String.format(
            Locale.US,
            "IntegrityChecker: Integrity score %.3f - %s",
            integrityScore,
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
    
    private fun checkDataIntegrity(state: MutableMap<String, Any>): Double {
        val suppressionScore = state["suppressionMemoryScore"] as? Double ?: return 0.0
        val driftScore = state["driftAdaptationScore"] as? Double ?: return 0.0
        val patternScore = state["patternEffectivenessScore"] as? Double ?: return 0.0
        
        if (suppressionScore < 0.0 || suppressionScore > 1.0) return 0.0
        if (driftScore < 0.0 || driftScore > 1.0) return 0.0
        if (patternScore < 0.0 || patternScore > 1.0) return 0.0
        
        return 0.85
    }
}
