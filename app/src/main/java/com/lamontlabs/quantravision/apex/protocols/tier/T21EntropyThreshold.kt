package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class T21EntropyThreshold : ApexProtocol {
    override val protocolId = "T21"
    override val protocolName = "EntropyThreshold"
    override val weight = 1.6
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["entropyThresholdOk"] = false
            state["normalizedEntropy"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "EntropyThreshold: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val entropyScore = state["aggregatedEntropyScore"] as? Double ?: 0.5
        val conflictCount = state["conflictCount"] as? Int ?: 0
        
        val prices = primitives.candles.map { it.close }
        val mean = prices.average()
        val variance = prices.map { (it - mean).pow(2) }.average()
        val stdDev = sqrt(variance)
        val normalizedEntropy = if (mean > 0.0) (stdDev / mean).coerceIn(0.0, 1.0) else 0.5
        
        val adjustedEntropy = (entropyScore * 0.7) + (normalizedEntropy * 0.3) + (conflictCount * 0.05)
        val finalEntropy = adjustedEntropy.coerceIn(0.0, 1.0)
        
        state["entropyThresholdOk"] = finalEntropy < 0.7
        state["normalizedEntropy"] = normalizedEntropy
        
        val passed = finalEntropy < 0.7
        val confidence = if (passed) (1.0 - finalEntropy).coerceIn(0.0, 1.0) else 0.2
        
        val reason = String.format(
            Locale.US,
            "EntropyThreshold: %.2f (threshold 0.7) - %s (conflicts=%d, normalized=%.2f)",
            finalEntropy,
            if (passed) "PASS" else "FAIL",
            conflictCount,
            normalizedEntropy
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
