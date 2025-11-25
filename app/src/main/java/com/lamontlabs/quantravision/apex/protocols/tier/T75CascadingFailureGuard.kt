package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T75CascadingFailureGuard : ApexProtocol {
    override val protocolId = "T75"
    override val protocolName = "CascadingFailureGuard"
    override val weight = 3.4
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["cascadingFailureRisk"] = 1.0
            state["cascadeScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "CascadingFailureGuard: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val flashCrashDetected = state["flashCrashDetected"] as? Boolean ?: true
        val liquidityStress = state["liquidityStress"] as? Boolean ?: true
        val riskAmplified = state["riskAmplified"] as? Boolean ?: true
        val marketStressLevel = state["marketStressLevel"] as? Double ?: 1.0
        
        val riskFlagCount = listOf(
            flashCrashDetected,
            liquidityStress,
            riskAmplified,
            marketStressLevel >= 0.7
        ).count { it }
        
        val cascadeScore = riskFlagCount / 4.0
        val cascadingFailureRisk = cascadeScore
        
        state["cascadingFailureRisk"] = cascadingFailureRisk
        state["cascadeScore"] = cascadeScore
        
        val passed = cascadeScore < 0.5
        val confidence = 1.0 - cascadeScore
        
        val reason = String.format(
            Locale.US,
            "CascadingFailureGuard: %.2f (%d/4 risk flags) - %s",
            cascadeScore,
            riskFlagCount,
            if (passed) "PASS" else "CASCADING_RISK"
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
