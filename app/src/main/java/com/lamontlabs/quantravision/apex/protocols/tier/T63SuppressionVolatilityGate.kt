package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T63SuppressionVolatilityGate : ApexProtocol {
    override val protocolId = "T63"
    override val protocolName = "SuppressionVolatilityGate"
    override val weight = 3.1
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["gateOpen"] = false
            state["gateScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "SuppressionVolatilityGate: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val suppressionActive = state["suppressionActive"] as? Boolean ?: true
        val volatilityException = state["volatilityException"] as? Boolean ?: true
        val marketStressLevel = state["marketStressLevel"] as? Double ?: 0.8
        
        val gateOpen = !suppressionActive && !volatilityException && marketStressLevel < 0.7
        val gateScore = if (gateOpen) 1.0 else 0.0
        
        state["gateOpen"] = gateOpen
        state["gateScore"] = gateScore
        
        val passed = gateOpen
        val confidence = if (gateOpen) 1.0 - marketStressLevel else 0.0
        
        val reason = String.format(
            Locale.US,
            "SuppressionVolatilityGate: %s (supp=%s, volEx=%s, stress=%.2f) - %s",
            if (gateOpen) "OPEN" else "CLOSED",
            suppressionActive,
            volatilityException,
            marketStressLevel,
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
