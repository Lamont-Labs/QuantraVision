package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T80FinalVerdictFusion : ApexProtocol {
    override val protocolId = "T80"
    override val protocolName = "FinalVerdictFusion"
    override val weight = 3.5
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["finalVerdict"] = false
            state["finalConfidence"] = 0.0
            state["finalScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "FinalVerdictFusion: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val preVerdictPassed = state["preVerdictPassed"] as? Boolean ?: false
        val normalizedScore = state["normalizedScore"] as? Double ?: 0.0
        val proofReady = state["proofReady"] as? Boolean ?: false
        val suppressionActive = state["suppressionActive"] as? Boolean ?: true
        val cascadingFailureRisk = state["cascadingFailureRisk"] as? Double ?: 1.0
        
        // FAIL-CLOSED: Verify proof artifacts exist and are not placeholders
        val proofHash = state["proofHash"] as? String ?: ""
        val proofFingerprint = state["proofFingerprint"] as? String ?: ""
        val proofGenerationToken = state["proofGenerationToken"] as? String ?: ""
        
        val invalidProofArtifacts = proofHash.isEmpty() || 
                                    proofFingerprint.isEmpty() ||
                                    proofGenerationToken.isEmpty() ||
                                    proofHash == "hash_error" ||
                                    proofHash == "incomplete_state" ||
                                    proofFingerprint == "empty" ||
                                    proofFingerprint == "missing_dependencies"
        
        if (invalidProofArtifacts) {
            state["finalVerdict"] = false
            state["finalConfidence"] = 0.0
            state["finalScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = String.format(
                    Locale.US,
                    "FinalVerdictFusion: Invalid proof artifacts (hash=%s, fingerprint=%s, token=%s) - FAIL (fail-closed)",
                    if (proofHash.isEmpty() || proofHash == "hash_error" || proofHash == "incomplete_state") "INVALID" else "OK",
                    if (proofFingerprint.isEmpty() || proofFingerprint == "empty" || proofFingerprint == "missing_dependencies") "INVALID" else "OK",
                    if (proofGenerationToken.isEmpty()) "MISSING" else "OK"
                ),
                weight = weight
            )
        }
        
        // Final verdict with all gates
        val finalVerdict = preVerdictPassed && proofReady &&
                          !suppressionActive && cascadingFailureRisk < 0.5
        
        val finalConfidence = if (finalVerdict) normalizedScore / 100.0 else 0.0
        val finalScore = if (finalVerdict) normalizedScore else 0.0
        
        state["finalVerdict"] = finalVerdict
        state["finalConfidence"] = finalConfidence
        state["finalScore"] = finalScore
        
        val passed = finalVerdict
        val confidence = finalConfidence
        
        val reason = String.format(
            Locale.US,
            "FinalVerdictFusion: %s (score=%.1f, pre=%s, proof=%s, supp=%s, cascade=%.2f) - %s",
            if (finalVerdict) "APPROVED" else "REJECTED",
            finalScore,
            preVerdictPassed,
            proofReady,
            suppressionActive,
            cascadingFailureRisk,
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
}
