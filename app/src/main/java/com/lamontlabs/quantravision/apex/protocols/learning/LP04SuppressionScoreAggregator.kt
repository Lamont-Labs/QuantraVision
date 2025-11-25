package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP04SuppressionScoreAggregator : ApexProtocol {
    override val protocolId = "LP04"
    override val protocolName = "SuppressionScoreAggregator"
    override val weight = 1.15
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val reconciled = state["falsePositiveReconciled"] as? Boolean ?: false
        val decayCalculated = state["suppressionDecayCalculated"] as? Boolean ?: false
        
        if (!reconciled || !decayCalculated) {
            state["suppressionMemoryScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.2,
                reason = "SuppressionScoreAggregator: Missing upstream components - FAIL",
                weight = weight
            )
        }
        
        val reconciliationScore = state["reconciliationScore"] as? Double ?: 0.0
        val decayFactor = state["suppressionDecayFactor"] as? Double ?: 0.0
        
        val aggregatedScore = (reconciliationScore + decayFactor) / 2.0
        
        state["suppressionMemoryScore"] = aggregatedScore
        
        val passed = aggregatedScore > 0.4
        val confidence = aggregatedScore
        
        val reason = String.format(
            Locale.US,
            "SuppressionScoreAggregator: Aggregated score %.3f - %s",
            aggregatedScore,
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
