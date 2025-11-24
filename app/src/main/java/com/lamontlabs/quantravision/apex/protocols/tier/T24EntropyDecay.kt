package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T24EntropyDecay : ApexProtocol {
    override val protocolId = "T24"
    override val protocolName = "EntropyDecay"
    override val weight = 1.9
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["entropyDecayRate"] = 0.0
            state["entropyImproving"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "EntropyDecay: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val initialEntropy = state["entropyEarlyScore"] as? Double ?: 0.5
        val finalEntropy = state["aggregatedEntropyScore"] as? Double ?: initialEntropy
        
        val entropyDecayRate = if (initialEntropy > 0.0) {
            ((initialEntropy - finalEntropy) / initialEntropy).coerceIn(-1.0, 1.0)
        } else {
            0.0
        }
        
        val entropyImproving = entropyDecayRate > 0.0
        
        state["entropyDecayRate"] = entropyDecayRate
        state["entropyImproving"] = entropyImproving
        
        val passed = entropyImproving || (finalEntropy < 0.5)
        val confidence = if (entropyImproving) {
            (0.5 + (entropyDecayRate * 0.5)).coerceIn(0.0, 1.0)
        } else {
            (1.0 - finalEntropy).coerceIn(0.0, 0.5)
        }
        
        val reason = String.format(
            Locale.US,
            "EntropyDecay: rate=%.2f (initial=%.2f, final=%.2f) - %s",
            entropyDecayRate,
            initialEntropy,
            finalEntropy,
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
