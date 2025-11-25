package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T54ConflictSuppression : ApexProtocol {
    override val protocolId = "T54"
    override val protocolName = "ConflictSuppression"
    override val weight = 2.9
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["conflictSuppressed"] = true
            state["conflictSuppressionScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ConflictSuppression: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val conflictCount = state["conflictCount"] as? Int ?: 5
        val unresolvedConflicts = state["unresolvedConflicts"] as? Int ?: 3
        
        val shouldSuppress = unresolvedConflicts > 2 || conflictCount > 4
        
        val conflictSuppressionScore = when {
            unresolvedConflicts > 4 -> 0.95
            unresolvedConflicts > 2 -> 0.9
            conflictCount > 6 -> 0.85
            conflictCount > 4 -> 0.8
            conflictCount > 2 -> 0.6
            else -> 0.2
        }
        
        state["conflictSuppressed"] = shouldSuppress
        state["conflictSuppressionScore"] = conflictSuppressionScore
        
        val passed = !shouldSuppress
        val confidence = if (shouldSuppress) 0.0 else 0.8
        
        val reason = String.format(
            Locale.US,
            "ConflictSuppression: %.2f (unresolved=%d, total=%d) - %s",
            conflictSuppressionScore,
            unresolvedConflicts,
            conflictCount,
            if (passed) "PASS" else "SUPPRESSED"
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
