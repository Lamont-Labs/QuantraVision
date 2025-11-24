package com.lamontlabs.quantravision.apex

import com.lamontlabs.quantravision.apex.models.ApexResult
import com.lamontlabs.quantravision.apex.models.ProtocolVerdict
import java.security.MessageDigest

/**
 * BATCH 2: Proof Hasher
 * 
 * Cryptographic audit trail for Apex Engine results.
 * Generates deterministic SHA-256 hashes of canonicalized ApexResult payloads.
 * 
 * Purpose:
 * - Verify integrity of Apex verdicts
 * - Support cloud narration packet validation
 * - Enable tamper-proof audit logs
 * - Ensure reproducible hashing across sessions
 * 
 * Architecture:
 * - Canonical JSON-like serialization (stable field order)
 * - SHA-256 hashing via java.security.MessageDigest
 * - Hex encoding for human-readable hashes
 */
object ProofHasher {
    
    private const val HASH_ALGORITHM = "SHA-256"
    
    /**
     * Generate deterministic proof hash for ApexResult.
     * 
     * DETERMINISM GUARANTEE:
     * - Identical analysis content → identical proof hash (regardless of timestamp)
     * - Hash EXCLUDES scanId and timestamp (temporal/uniqueness fields)
     * - Hash INCLUDES only canonical content (status, score, trace, entropy, etc.)
     * 
     * Purpose: Enable reproducible verification and audit trails.
     * Same inputs (primitives + analysis) always produce same proof hash.
     * 
     * @param result Apex Engine result to hash
     * @return Hex-encoded SHA-256 hash (content-only, timestamp-independent)
     */
    fun hashApexResult(result: ApexResult): String {
        val canonical = canonicalizeApexResult(result)
        return sha256Hex(canonical)
    }
    
    /**
     * Generate proof hash for protocol trace only.
     * Useful for verifying trace integrity without full result.
     * 
     * @param trace List of protocol verdicts
     * @return Hex-encoded SHA-256 hash
     */
    fun hashProtocolTrace(trace: List<ProtocolVerdict>): String {
        val canonical = canonicalizeProtocolTrace(trace)
        return sha256Hex(canonical)
    }
    
    /**
     * Canonicalize ApexResult into stable string representation FOR PROOF HASH.
     * 
     * CRITICAL DETERMINISM REQUIREMENT:
     * - EXCLUDES scanId (contains timestamp, not deterministic)
     * - EXCLUDES timestamp (temporal field, not part of content)
     * - Includes ONLY canonical content fields for deterministic verification
     * 
     * Purpose: Enable "same inputs → same outputs" verification.
     * Identical analysis content at different times produces IDENTICAL proof hash.
     * 
     * Field order is fixed to ensure reproducibility.
     * 
     * Format (pseudo-JSON, no whitespace):
     * {status:...,quantraScore:...,trace:[...],entropy:...,suppression:...,omega:...,regime:...,confidence:...}
     */
    private fun canonicalizeApexResult(result: ApexResult): String {
        val traceCanonical = canonicalizeProtocolTrace(result.protocolTrace)
        
        return buildString {
            append("{")
            append("status:${result.status.name},")
            append("quantraScore:${result.quantraScore.normalizedScore},")
            append("band:${result.quantraScore.band.name},")
            append("trace:$traceCanonical,")
            append("entropy:${formatDouble(result.entropyScore)},")
            append("suppression:${result.suppressionActive},")
            append("omega:${result.omegaLock},")
            append("regime:${result.regimeOk},")
            append("confidence:${formatDouble(result.confidenceApex)}")
            append("}")
        }
    }
    
    /**
     * Canonicalize protocol trace into stable string representation.
     * 
     * Format: [id:T01,pass:true,conf:0.85;id:T02,pass:false,conf:0.32;...]
     */
    private fun canonicalizeProtocolTrace(trace: List<ProtocolVerdict>): String {
        if (trace.isEmpty()) return "[]"
        
        return buildString {
            append("[")
            trace.forEachIndexed { index, verdict ->
                if (index > 0) append(";")
                append("id:${verdict.protocolId},")
                append("pass:${verdict.passed},")
                append("conf:${formatDouble(verdict.confidence)}")
            }
            append("]")
        }
    }
    
    /**
     * Format double to 6 decimal places for canonical representation.
     * Prevents floating-point precision drift in hash calculation.
     */
    private fun formatDouble(value: Double): String {
        return "%.6f".format(value)
    }
    
    /**
     * Calculate SHA-256 hash and return as hex string.
     * 
     * @param input Canonical string representation
     * @return Hex-encoded hash (64 characters)
     */
    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verify proof hash matches expected hash.
     * 
     * @param result ApexResult to verify
     * @param expectedHash Expected proof hash
     * @return True if hashes match, false otherwise
     */
    fun verifyProofHash(result: ApexResult, expectedHash: String): Boolean {
        val actualHash = hashApexResult(result)
        return actualHash.equals(expectedHash, ignoreCase = true)
    }
    
    /**
     * Generate deterministic scan ID with embedded timestamp and context hash.
     * Format: APEX_<timestamp_ms>_<deterministic_hash>
     * 
     * The deterministic suffix is derived from SHA-256 hash of timestamp + contextData,
     * ensuring identical inputs always produce identical scan IDs for reproducible
     * proof hashes and audit trails.
     * 
     * @param timestamp Scan timestamp in milliseconds
     * @param contextData Canonical context string (ticker, timeframe, chartType, imageHash, etc.)
     *                    Default empty string for backward compatibility
     * @return Deterministic scan identifier
     */
    fun generateScanId(timestamp: Long, contextData: String = ""): String {
        val inputString = "$timestamp$contextData"
        val hashHex = sha256Hex(inputString)
        val deterministicSuffix = hashHex.substring(0, 8)
        
        return "APEX_${timestamp}_$deterministicSuffix"
    }
}
