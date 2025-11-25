package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T31DriftDetectionEarly : ApexProtocol {
    override val protocolId = "T31"
    override val protocolName = "DriftDetectionEarly"
    override val weight = 1.8
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["driftRisk"] = 0.0
            state["priceDriftBaseline"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "DriftDetectionEarly: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val allPrices = primitives.candles.map { it.close }
        val overallAvg = if (allPrices.isNotEmpty()) allPrices.average() else 0.0
        
        val rolling10Prices = primitives.candles.takeLast(10).map { it.close }
        val rolling10Avg = if (rolling10Prices.isNotEmpty()) rolling10Prices.average() else 0.0
        
        val priceDriftBaseline = overallAvg
        val driftRisk = if (overallAvg > 0.0) {
            abs(rolling10Avg - overallAvg) / overallAvg
        } else 0.0
        
        state["driftRisk"] = driftRisk
        state["priceDriftBaseline"] = priceDriftBaseline
        
        val passed = driftRisk < 0.15
        val confidence = (1.0 - (driftRisk / 0.15)).coerceIn(0.0, 1.0)
        
        val reason = String.format(
            Locale.US,
            "DriftDetectionEarly: risk=%.2f (threshold 0.15) - %s (baseline=%.2f)",
            driftRisk,
            if (passed) "PASS" else "FAIL",
            priceDriftBaseline
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
