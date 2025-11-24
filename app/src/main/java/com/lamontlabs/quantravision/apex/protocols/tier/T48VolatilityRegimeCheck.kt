package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T48VolatilityRegimeCheck : ApexProtocol {
    override val protocolId = "T48"
    override val protocolName = "VolatilityRegimeCheck"
    override val weight = 2.7
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["volatilityRegimeMatch"] = false
            state["volatilityRegimeScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "VolatilityRegimeCheck: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val volatility = state["volatility"] as? Double ?: 0.05
        val atr = state["atr"] as? Double ?: 1.0
        val volatilityBaseline = state["volatilityBaseline"] as? Double ?: 0.05
        
        val tolerance = 0.3
        val volatilityDiff = if (volatilityBaseline > 0.0) {
            abs(volatility - volatilityBaseline) / volatilityBaseline
        } else {
            1.0
        }
        
        val volatilityRegimeMatch = volatilityDiff <= tolerance
        
        val volatilityRegimeScore = if (volatilityRegimeMatch) {
            (1.0 - (volatilityDiff / tolerance)).coerceIn(0.0, 1.0)
        } else {
            (0.5 * (1.0 - volatilityDiff)).coerceIn(0.0, 0.5)
        }
        
        state["volatilityRegimeMatch"] = volatilityRegimeMatch
        state["volatilityRegimeScore"] = volatilityRegimeScore
        
        val passed = volatilityRegimeMatch
        val confidence = volatilityRegimeScore
        
        val reason = String.format(
            Locale.US,
            "VolatilityRegimeCheck: %.2f (current=%.3f, baseline=%.3f, diff=%.1f%%) - %s",
            volatilityRegimeScore,
            volatility,
            volatilityBaseline,
            volatilityDiff * 100,
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
