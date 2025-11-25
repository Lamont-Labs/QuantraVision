package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*

class Omega02RiskCapEnforcer : ApexProtocol {
    override val protocolId = "Omega02"
    override val protocolName = "RiskCapEnforcer"
    override val weight = 4.8
    
    companion object {
        private const val MAX_QUANTRA_SCORE = 100.0
        private const val MAX_CONFIDENCE = 1.0
        private const val OVER_LEVERAGED_THRESHOLD = 0.95
        private const val MAX_POSITION_SIZE_PCT = 10.0
    }
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        // FAIL-CLOSED: Check candles
        if (primitives.candles.size < 10) {
            state["omega02_passed"] = false
            state["omega02_riskViolations"] = emptyList<String>()
            state["omega02_reason"] = "RiskCapEnforcer: Insufficient candles - FAIL"
            
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "Omega02: FAIL - Insufficient candles for risk analysis",
                weight = weight
            )
        }
        
        // FAIL-CLOSED: Verify at least one risk metric exists for validation
        val quantraScore = state["quantraScore"] as? Double
        val confidence = state["confidence"] as? Double
        val positionSize = state["positionSize"] as? Double
        
        // If NO risk metrics available, FAIL (cannot validate risk without data)
        if (quantraScore == null && confidence == null && positionSize == null) {
            val violations = listOf("No risk metrics available for validation")
            state["omega02_passed"] = false
            state["omega02_riskViolations"] = violations
            state["omega02_reason"] = "RiskCapEnforcer: Missing all risk metrics (quantraScore, confidence, positionSize) - FAIL (fail-closed)"
            
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "Omega02: FAIL - Missing all risk metrics (quantraScore, confidence, positionSize) - fail-closed",
                weight = weight
            )
        }
        
        // Now validate available metrics
        val violations = mutableListOf<String>()
        
        // Check QuantraScore if present
        if (quantraScore != null && quantraScore > MAX_QUANTRA_SCORE) {
            violations.add("QuantraScore exceeds limit: $quantraScore > $MAX_QUANTRA_SCORE")
        }
        
        // Check confidence if present
        if (confidence != null && confidence > MAX_CONFIDENCE) {
            violations.add("Confidence exceeds limit: $confidence > $MAX_CONFIDENCE")
        }
        if (confidence != null && confidence > OVER_LEVERAGED_THRESHOLD) {
            violations.add("Over-leveraged signal detected: confidence=$confidence")
        }
        
        // Check position size if present
        if (positionSize != null && positionSize > MAX_POSITION_SIZE_PCT) {
            violations.add("Position size exceeds limit: $positionSize > $MAX_POSITION_SIZE_PCT")
        }
        
        val passed = violations.isEmpty()
        state["omega02_passed"] = passed
        state["omega02_riskViolations"] = violations
        
        val reason = if (passed) {
            "RiskCapEnforcer: All risk caps validated - PASS"
        } else {
            "RiskCapEnforcer: Risk violations detected (${violations.size}) - FAIL"
        }
        state["omega02_reason"] = reason
        
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = passed,
            confidence = if (passed) 1.0 else 0.0,
            reason = if (passed) {
                "Omega02: PASS - All risk limits within acceptable bounds"
            } else {
                "Omega02: FAIL - ${violations.size} risk violations: ${violations.joinToString(", ")}"
            },
            weight = weight
        )
    }
}
