package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T49TemporalRegimeStability : ApexProtocol {
    override val protocolId = "T49"
    override val protocolName = "TemporalRegimeStability"
    override val weight = 2.8
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["regimeStabilityScore"] = 0.0
            state["regimeStable"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "TemporalRegimeStability: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val regimeShiftDetected = state["regimeShiftDetected"] as? Boolean ?: false
        val marketRegime = state["marketRegime"] as? String ?: "UNKNOWN"
        
        val recentWindowSize = (primitives.candles.size * 0.2).toInt().coerceAtLeast(6)
        
        val regimeStable = !regimeShiftDetected && marketRegime != "UNKNOWN"
        
        val regimeStabilityScore = when {
            regimeStable && marketRegime in setOf("TRENDING", "RANGING") -> 0.85
            regimeStable -> 0.70
            !regimeShiftDetected -> 0.55
            else -> 0.30
        }
        
        state["regimeStabilityScore"] = regimeStabilityScore
        state["regimeStable"] = regimeStable
        
        val passed = regimeStable
        val confidence = regimeStabilityScore
        
        val reason = String.format(
            Locale.US,
            "TemporalRegimeStability: %.2f (regime=%s, shift=%s, window=%d) - %s",
            regimeStabilityScore,
            marketRegime,
            regimeShiftDetected,
            recentWindowSize,
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
