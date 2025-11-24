package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class T33AdaptiveThreshold : ApexProtocol {
    override val protocolId = "T33"
    override val protocolName = "AdaptiveThreshold"
    override val weight = 2.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["adaptiveThreshold"] = 0.5
            state["thresholdAdjustment"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "AdaptiveThreshold: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val recentPrices = primitives.candles.takeLast(10).map { it.close }
        val mean = if (recentPrices.isNotEmpty()) recentPrices.average() else 0.0
        val variance = if (recentPrices.isNotEmpty()) recentPrices.map { (it - mean).pow(2) }.average() else 0.0
        val stdDev = sqrt(variance)
        
        val baseThreshold = 0.5
        val volatilityFactor = if (mean > 0.0) (stdDev / mean).coerceIn(0.0, 1.0) else 0.0
        
        val thresholdAdjustment = volatilityFactor * 0.3
        val adaptiveThreshold = (baseThreshold + thresholdAdjustment).coerceIn(0.3, 0.9)
        
        state["adaptiveThreshold"] = adaptiveThreshold
        state["thresholdAdjustment"] = thresholdAdjustment
        
        val passed = adaptiveThreshold < 0.75
        val confidence = (1.0 - adaptiveThreshold).coerceIn(0.0, 1.0)
        
        val reason = String.format(
            Locale.US,
            "AdaptiveThreshold: %.2f (base=%.2f, adjustment=%.2f) - %s",
            adaptiveThreshold,
            baseThreshold,
            thresholdAdjustment,
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
