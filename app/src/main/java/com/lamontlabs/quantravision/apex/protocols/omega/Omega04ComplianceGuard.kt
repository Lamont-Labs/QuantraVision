package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*

class Omega04ComplianceGuard : ApexProtocol {
    override val protocolId = "Omega04"
    override val protocolName = "ComplianceGuard"
    override val weight = 4.7
    
    companion object {
        private const val FREE_TIER_DAILY_SCAN_LIMIT = 3
        private val RESTRICTED_COUNTRIES = setOf("XX", "YY")
    }
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val violations = mutableListOf<String>()
        
        if (primitives.candles.size < 10) {
            state["omega04_passed"] = false
            state["omega04_complianceViolations"] = listOf("Insufficient candles")
            state["omega04_reason"] = "Insufficient candles for compliance checks"
            
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "Omega04: FAIL - Insufficient candles for compliance checks",
                weight = weight
            )
        }
        
        val disclaimerAcknowledged = state["disclaimerAcknowledged"] as? Boolean
        if (disclaimerAcknowledged != true) {
            violations.add("Disclaimer not acknowledged")
        }
        
        if (context.tier == SubscriptionTier.FREE) {
            val dailyScanCount = state["dailyScanCount"] as? Int
            if (dailyScanCount != null && dailyScanCount > FREE_TIER_DAILY_SCAN_LIMIT) {
                violations.add("FREE tier daily scan limit exceeded ($dailyScanCount > $FREE_TIER_DAILY_SCAN_LIMIT)")
            }
            
            val detectedPattern = state["detectedPattern"] as? String
            if (detectedPattern != null && detectedPattern.contains("exotic", ignoreCase = true)) {
                violations.add("FREE tier cannot access exotic patterns")
            }
        }
        
        val countryCode = state["countryCode"] as? String
        if (countryCode != null && countryCode in RESTRICTED_COUNTRIES) {
            violations.add("Access restricted in country: $countryCode")
        }
        
        val passed = violations.isEmpty()
        state["omega04_passed"] = passed
        state["omega04_complianceViolations"] = violations
        state["omega04_reason"] = if (passed) {
            "All compliance checks passed"
        } else {
            violations.joinToString("; ")
        }
        
        val reason = if (passed) {
            "Omega04: PASS - All compliance requirements satisfied"
        } else {
            "Omega04: FAIL - ${violations.size} compliance violations: ${violations.joinToString(", ")}"
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
