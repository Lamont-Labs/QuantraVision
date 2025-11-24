package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T23ConflictResolution : ApexProtocol {
    override val protocolId = "T23"
    override val protocolName = "ConflictResolution"
    override val weight = 1.8
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 15) {
            state["resolvedConflictCount"] = 0
            state["unresolvedConflicts"] = 0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ConflictResolution: Insufficient candles (need >=15, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        @Suppress("UNCHECKED_CAST")
        val conflictFlags = state["conflictFlags"] as? List<String> ?: emptyList()
        val conflictCount = state["conflictCount"] as? Int ?: 0
        
        val trendStrength = state["trendStrength"] as? Double ?: 0.0
        val trendAligned = trendStrength >= 0.5
        val momentumAligned = state["momentumAligned"] as? Boolean ?: true
        val volumeConfirmed = state["volumeConfirmed"] as? Boolean ?: true
        
        var resolvableCount = 0
        if (trendAligned && momentumAligned) resolvableCount++
        if (volumeConfirmed && trendAligned) resolvableCount++
        
        val resolvedConflictCount = (conflictCount * resolvableCount / 3).coerceAtMost(conflictCount)
        val unresolvedConflicts = conflictCount - resolvedConflictCount
        
        state["resolvedConflictCount"] = resolvedConflictCount
        state["unresolvedConflicts"] = unresolvedConflicts
        
        val passed = unresolvedConflicts <= 2
        val confidence = if (conflictCount == 0) 0.95 else (1.0 - (unresolvedConflicts.toDouble() / (conflictCount + 1))).coerceIn(0.0, 1.0)
        
        val reason = String.format(
            Locale.US,
            "ConflictResolution: %d resolved, %d unresolved (total=%d) - %s",
            resolvedConflictCount,
            unresolvedConflicts,
            conflictCount,
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
