package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.max
/**
 * T17: ConflictDetection
 * Purpose: Detects conflicting signals across indicators
 * Category: Entropy & Conflict Detection
 * 
 * BATCH 8 FIX: Safe state access, fully deterministic
 */
class T17ConflictDetection : ApexProtocol {
    override val protocolId = "T17"
    override val protocolName = "ConflictDetection"
    override val weight = 1.5
    
    override fun execute(primitives: ApexPrimitives, state: MutableMap<String, Any>): ProtocolVerdict {
        if (primitives.candles.isEmpty()) {
            state["conflictFlags"] = emptyList<String>()
            state["conflictCount"] = 0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ConflictDetection: No candle data available",
                weight = weight
            )
        }
        
        val conflictFlags = mutableListOf<String>()
        
        // Safe state access - check momentum vs volume conflict
        val momentumAligned = state["momentumAligned"] as? Boolean
        val volumeConfirmed = state["volumeConfirmed"] as? Boolean
        if (momentumAligned == true && volumeConfirmed == false) {
            conflictFlags.add("momentum-volume-divergence")
        } else if (momentumAligned == false && volumeConfirmed == true) {
            conflictFlags.add("volume-momentum-conflict")
        }
        // Safe state access - check trend vs volatility conflict
        val trendStrength = state["trendStrength"] as? Double ?: 0.5
        val volatilityState = state["volatility"] as? String ?: "normal"
        if (trendStrength > 0.7 && volatilityState == "low") {
            conflictFlags.add("strong-trend-low-volatility")
        } else if (trendStrength < 0.3 && volatilityState == "high") {
            conflictFlags.add("weak-trend-high-volatility")
        }
        
        // Safe state access - check MTF coherence conflict
        val mtfCoherent = state["mtfCoherent"] as? Boolean
        if (mtfCoherent == false) {
            conflictFlags.add("multi-timeframe-conflict")
        }
        
        // Safe state access - check price action vs structure conflict
        val priceActionQuality = state["priceActionQuality"] as? Double ?: 0.5
        val structureComplete = state["structureComplete"] as? Boolean ?: true
        if (priceActionQuality < 0.4 && structureComplete == true) {
            conflictFlags.add("poor-price-action-complete-structure")
        }
        
        state["conflictFlags"] = conflictFlags
        state["conflictCount"] = conflictFlags.size
        val majorConflictCount = conflictFlags.size
        val passed = majorConflictCount == 0
        val confidence = max(0.2, 1.0 - majorConflictCount * 0.25)
        val details = if (conflictFlags.isNotEmpty()) {
            conflictFlags.take(3).joinToString(", ")
        } else {
            "none"
        }
        
        val reason = "Conflicts: $majorConflictCount - $details"
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
