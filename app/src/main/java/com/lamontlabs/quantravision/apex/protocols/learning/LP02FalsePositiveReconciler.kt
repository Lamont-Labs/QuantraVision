package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP02FalsePositiveReconciler : ApexProtocol {
    override val protocolId = "LP02"
    override val protocolName = "FalsePositiveReconciler"
    override val weight = 1.05
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val suppressionActive = state["suppressionActive"] as? Boolean ?: false
        val finalSuppressionScore = state["finalSuppressionScore"] as? Double ?: 0.0
        
        if (!suppressionActive) {
            state["falsePositiveReconciled"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.3,
                reason = "FalsePositiveReconciler: No suppression flags from T51-T55 - FAIL",
                weight = weight
            )
        }
        
        val reconciliationScore = reconcileSuppressionFlags(state, finalSuppressionScore)
        
        state["falsePositiveReconciled"] = true
        state["reconciliationScore"] = reconciliationScore
        
        val passed = reconciliationScore > 0.5
        val confidence = reconciliationScore
        
        val reason = String.format(
            Locale.US,
            "FalsePositiveReconciler: Reconciliation score %.2f - %s",
            reconciliationScore,
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
    
    private fun reconcileSuppressionFlags(state: MutableMap<String, Any>, suppressionScore: Double): Double {
        return suppressionScore.coerceIn(0.0, 1.0)
    }
}
