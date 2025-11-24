package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T32RegimeShift : ApexProtocol {
    override val protocolId = "T32"
    override val protocolName = "RegimeShift"
    override val weight = 1.9
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["regimeShiftDetected"] = false
            state["volatilityBaseline"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "RegimeShift: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val allRanges = primitives.candles.map { it.high - it.low }
        val overallATR = if (allRanges.isNotEmpty()) allRanges.average() else 0.0
        
        val recentRanges = primitives.candles.takeLast(10).map { it.high - it.low }
        val recentATR = if (recentRanges.isNotEmpty()) recentRanges.average() else 0.0
        
        val volatilityBaseline = overallATR
        val atrRatio = if (overallATR > 0.0) recentATR / overallATR else 1.0
        
        val regimeShiftDetected = atrRatio > 1.5 || atrRatio < 0.5
        
        state["regimeShiftDetected"] = regimeShiftDetected
        state["volatilityBaseline"] = volatilityBaseline
        
        val passed = !regimeShiftDetected
        val confidence = if (passed) 0.85 else 0.3
        
        val reason = String.format(
            Locale.US,
            "RegimeShift: %s - %s (ATR ratio=%.2f, baseline=%.4f)",
            if (regimeShiftDetected) "DETECTED" else "STABLE",
            if (passed) "PASS" else "FAIL",
            atrRatio,
            volatilityBaseline
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
