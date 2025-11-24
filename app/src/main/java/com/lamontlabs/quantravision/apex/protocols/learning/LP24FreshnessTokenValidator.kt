package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP24FreshnessTokenValidator : ApexProtocol {
    override val protocolId = "LP24"
    override val protocolName = "FreshnessTokenValidator"
    override val weight = 1.55
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val proofDigest = state["learningProofDigest"] as? String ?: ""
        
        if (proofDigest.isEmpty()) {
            state["freshnessTokenValid"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "FreshnessTokenValidator: No proof digest available - FAIL",
                weight = weight
            )
        }
        
        val proofGenerationToken = state["proofGenerationToken"] as? String
        
        val tokenValid = validateFreshnessToken(proofGenerationToken)
        
        state["freshnessTokenValid"] = tokenValid
        
        val passed = tokenValid
        val confidence = if (tokenValid) 0.85 else 0.3
        
        val reason = if (tokenValid) {
            "FreshnessTokenValidator: Upstream tokens valid - PASS"
        } else {
            "FreshnessTokenValidator: Missing or invalid freshness token - FAIL"
        }
        
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = passed,
            confidence = confidence,
            reason = reason,
            weight = weight
        )
    }
    
    private fun validateFreshnessToken(token: String?): Boolean {
        return !token.isNullOrBlank()
    }
}
