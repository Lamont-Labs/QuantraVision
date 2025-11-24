package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
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
        val violations = mutableListOf<String>()
        
        if (primitives.candles.size < 10) {
            state["omega02_passed"] = false
            state["omega02_riskViolations"] = listOf("Insufficient candles")
            state["omega02_reason"] = "Insufficient candles for risk analysis"
            
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "Omega02: FAIL - Insufficient candles for risk analysis",
                weight = weight
            )
        }
        
        val quantraScore = state["quantraScore"] as? Double
        if (quantraScore != null && quantraScore > MAX_QUANTRA_SCORE) {
            violations.add("QuantraScore exceeds max ($quantraScore > $MAX_QUANTRA_SCORE)")
        }
        
        val confidence = state["confidence"] as? Double
        if (confidence != null) {
            if (confidence > MAX_CONFIDENCE) {
                violations.add("Confidence exceeds max ($confidence > $MAX_CONFIDENCE)")
            }
            if (confidence > OVER_LEVERAGED_THRESHOLD) {
                violations.add("Over-leveraged confidence ($confidence > $OVER_LEVERAGED_THRESHOLD)")
            }
        }
        
        val positionSize = state["positionSize"] as? Double
        if (positionSize != null && positionSize > MAX_POSITION_SIZE_PCT) {
            violations.add("Position sizing exceeds 10% limit ($positionSize%)")
        }
        
        val passed = violations.isEmpty()
        state["omega02_passed"] = passed
        state["omega02_riskViolations"] = violations
        state["omega02_reason"] = if (passed) {
            "All risk caps satisfied"
        } else {
            violations.joinToString("; ")
        }
        
        val reason = if (passed) {
            "Omega02: PASS - All risk limits within acceptable bounds"
        } else {
            "Omega02: FAIL - ${violations.size} risk violations: ${violations.joinToString(", ")}"
        }
        
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = passed,
            confidence = if (passed) 1.0 else 0.0,
            reason = reason,
            weight = weight
        )
    }
}
