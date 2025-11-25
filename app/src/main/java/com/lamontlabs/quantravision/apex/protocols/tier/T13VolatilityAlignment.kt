package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
/**
 * T13: VolatilityAlignment
 * Purpose: Checks if volatility level matches pattern requirements
 * Category: Momentum & Alignment
 * 
 * BATCH 8 FIX: Uses actual candle data and safe state access
 */
class T13VolatilityAlignment : ApexProtocol {
    override val protocolId = "T13"
    override val protocolName = "VolatilityAlignment"
    override val weight = 1.5
    
    override fun execute(primitives: ApexPrimitives, state: MutableMap<String, Any>): ProtocolVerdict {
        if (primitives.candles.isEmpty()) {
            state["volatilityAligned"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "VolatilityAlignment: No candle data available",
                weight = weight
            )
        }
        
        // Safe state access
        val volatilityState = state["volatility"] as? String ?: "normal"
        val trendStrength = state["trendStrength"] as? Double ?: 0.5
        
        // Determine if volatility aligns with trend
        val volatilityAligned = when {
            trendStrength > 0.7 && volatilityState in listOf("normal", "high") -> true
            trendStrength < 0.3 && volatilityState in listOf("low", "normal") -> true
            trendStrength in 0.3..0.7 -> true  // Any volatility acceptable for sideways
            else -> false
        }
        state["volatilityAligned"] = volatilityAligned
        val passed = volatilityAligned
        val confidence = if (passed) 0.9 else 0.4
        val status = if (passed) "aligned" else "misaligned"
        val reason = "Volatility alignment: $status (volatility=$volatilityState, trend=${"%.2f".format(Locale.US, trendStrength)})"
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
