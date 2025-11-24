package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T64MultiSignalIntegration : ApexProtocol {
    override val protocolId = "T64"
    override val protocolName = "MultiSignalIntegration"
    override val weight = 3.15
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["signalsIntegrated"] = false
            state["integrationScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "MultiSignalIntegration: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val trendStrength = state["trendStrength"] as? Double ?: 0.0
        val momentumScore = state["momentumScore"] as? Double ?: 0.0
        val volumeConfirmationScore = state["volumeConfirmationScore"] as? Double ?: 0.0
        val structureComplete = state["structureComplete"] as? Boolean ?: false
        
        val integrationScore = 0.3 * trendStrength +
                              0.3 * momentumScore +
                              0.25 * volumeConfirmationScore +
                              0.15 * (if (structureComplete) 1.0 else 0.0)
        
        val signalsIntegrated = integrationScore >= 0.65
        
        state["signalsIntegrated"] = signalsIntegrated
        state["integrationScore"] = integrationScore
        
        val passed = integrationScore >= 0.65
        val confidence = integrationScore
        
        val reason = String.format(
            Locale.US,
            "MultiSignalIntegration: %.2f (trend=%.2f, mom=%.2f, vol=%.2f, struct=%s) - %s",
            integrationScore,
            trendStrength,
            momentumScore,
            volumeConfirmationScore,
            structureComplete,
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
