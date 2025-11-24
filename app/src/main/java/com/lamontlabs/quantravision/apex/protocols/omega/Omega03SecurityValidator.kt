package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*

class Omega03SecurityValidator : ApexProtocol {
    override val protocolId = "Omega03"
    override val protocolName = "SecurityValidator"
    override val weight = 4.9
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val violations = mutableListOf<String>()
        
        if (primitives.candles.size < 10) {
            state["omega03_passed"] = false
            state["omega03_securityViolations"] = listOf("Insufficient candles")
            state["omega03_reason"] = "Insufficient candles for security validation"
            
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "Omega03: FAIL - Insufficient candles for security validation",
                weight = weight
            )
        }
        
        if (context.userId.isEmpty()) {
            violations.add("User ID is empty")
        }
        
        val validTiers = setOf(SubscriptionTier.FREE, SubscriptionTier.BASIC, SubscriptionTier.PRO, SubscriptionTier.APEX)
        if (context.tier !in validTiers) {
            violations.add("Invalid subscription tier: ${context.tier}")
        }
        
        val proofDigest = state["proofDigest"] as? String
        if (proofDigest == null || proofDigest == "unknown" || proofDigest == "digest_error") {
            violations.add("Invalid or missing proofDigest")
        } else if (!isValidSHA256(proofDigest)) {
            violations.add("proofDigest is not a valid SHA-256 hash")
        }
        
        val learningProofDigest = state["learningProofDigest"] as? String
        if (learningProofDigest != null) {
            if (learningProofDigest == "unknown" || learningProofDigest == "digest_error") {
                violations.add("Invalid learningProofDigest")
            } else if (!isValidSHA256(learningProofDigest)) {
                violations.add("learningProofDigest is not a valid SHA-256 hash")
            }
        }
        
        val suspiciousKeys = listOf("__tampered__", "__override__", "__bypass__", "__inject__")
        for (key in state.keys) {
            if (suspiciousKeys.any { key.contains(it, ignoreCase = true) }) {
                violations.add("Suspicious state key detected: $key")
            }
        }
        
        val passed = violations.isEmpty()
        state["omega03_passed"] = passed
        state["omega03_securityViolations"] = violations
        state["omega03_reason"] = if (passed) {
            "All security checks passed"
        } else {
            violations.joinToString("; ")
        }
        
        val reason = if (passed) {
            "Omega03: PASS - All security validations satisfied"
        } else {
            "Omega03: FAIL - ${violations.size} security violations: ${violations.joinToString(", ")}"
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
    
    private fun isValidSHA256(hash: String): Boolean {
        if (hash.length != 64) return false
        return hash.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }
}
