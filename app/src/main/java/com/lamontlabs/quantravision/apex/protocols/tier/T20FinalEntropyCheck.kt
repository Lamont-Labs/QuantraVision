package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.max
import kotlin.math.min
/**
 * T20: FinalEntropyCheck
 * Purpose: Final entropy validation before scoring
 * Category: Entropy & Conflict Detection
 * 
 * BATCH 8 FIX: Safe state access, fully deterministic
 */
class T20FinalEntropyCheck : ApexProtocol {
    override val protocolId = "T20"
    override val protocolName = "FinalEntropyCheck"
    override val weight = 2.0
    
    override fun execute(primitives: ApexPrimitives, state: MutableMap<String, Any>): ProtocolVerdict {
        if (primitives.candles.isEmpty()) {
            state["finalEntropy"] = 1.0
            state["finalEntropyOk"] = false
            state["aggregatedEntropyScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "FinalEntropyCheck: No candle data available",
                weight = weight
            )
        }
        
        // Safe state access - aggregate entropy from all previous protocol results
        val earlyEntropy = state["entropyScore"] as? Double ?: 0.5
        val conflictCount = state["conflictCount"] as? Int ?: 0
        val signalClarity = state["signalClarity"] as? Double ?: 0.5
        val regimeMatch = state["regimeMatch"] as? Boolean ?: true
        
        // Calculate aggregate factors
        val conflictFactor = min(0.5, conflictCount * 0.15)
        val clarityFactor = (1.0 - signalClarity) * 0.3
        val regimeFactor = if (!regimeMatch) 0.2 else 0.0
        // Final entropy score (higher = more uncertainty)
        val finalEntropy = min(1.0, earlyEntropy * 0.4 + conflictFactor + clarityFactor + regimeFactor)
        // Calculate aggregated entropy score combining all factors
        val aggregatedEntropyScore = (earlyEntropy + finalEntropy) / 2.0
        state["finalEntropy"] = finalEntropy
        state["finalEntropyOk"] = finalEntropy < 0.60
        state["aggregatedEntropyScore"] = aggregatedEntropyScore
        // Classify entropy level
        val (status, passed, confidence) = when {
            finalEntropy > 0.70 -> Triple("FAIL", false, 0.1)
            finalEntropy > 0.60 -> Triple("WARN", false, 0.3)
            finalEntropy < 0.30 -> Triple("PASS", true, 0.95)
            else -> Triple("PASS", true, max(0.5, 1.0 - finalEntropy))
        }
        val components = "early=${"%.2f".format(Locale.US, earlyEntropy)}, " +
                        "conflicts=$conflictCount, " +
                        "clarity=${"%.2f".format(Locale.US, signalClarity)}, " +
                        "regime=${if(regimeMatch) "OK" else "MISMATCH"}"
        val reason = "Final entropy: ${"%.2f".format(Locale.US, finalEntropy)} - $status ($components)"
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
