package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T46RegimeAlignment : ApexProtocol {
    override val protocolId = "T46"
    override val protocolName = "RegimeAlignment"
    override val weight = 2.6
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["regimeAlignmentOk"] = false
            state["regimeAlignmentScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "RegimeAlignment: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val marketRegime = state["marketRegime"] as? String ?: "UNKNOWN"
        val regimeShiftDetected = state["regimeShiftDetected"] as? Boolean ?: false
        val volatilityBaseline = state["volatilityBaseline"] as? Double ?: 0.05
        
        val regimeStable = !regimeShiftDetected
        val regimeKnown = marketRegime != "UNKNOWN"
        
        val regimeAlignmentScore = when {
            regimeStable && regimeKnown -> 0.85
            regimeStable -> 0.65
            regimeKnown -> 0.55
            else -> 0.35
        }
        
        val regimeAlignmentOk = regimeAlignmentScore >= 0.6
        
        state["regimeAlignmentOk"] = regimeAlignmentOk
        state["regimeAlignmentScore"] = regimeAlignmentScore
        
        val passed = regimeAlignmentOk
        val confidence = regimeAlignmentScore
        
        val reason = String.format(
            Locale.US,
            "RegimeAlignment: %.2f (regime=%s, shift=%s, baseline=%.3f) - %s",
            regimeAlignmentScore,
            marketRegime,
            regimeShiftDetected,
            volatilityBaseline,
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
