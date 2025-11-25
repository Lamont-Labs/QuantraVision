package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T57VolatilitySpikeGuard : ApexProtocol {
    override val protocolId = "T57"
    override val protocolName = "VolatilitySpikeGuard"
    override val weight = 2.9
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["volatilitySpikeDetected"] = true
            state["spikeIntensity"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "VolatilitySpikeGuard: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val upstreamATR = state["atr"] as? Double
        
        if (upstreamATR == null) {
            state["volatilitySpikeDetected"] = true
            state["spikeIntensity"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "VolatilitySpikeGuard: Missing upstream ATR data - FAIL (fail-closed)",
                weight = weight
            )
        }
        
        val atr = upstreamATR
        
        val last10Candles = primitives.candles.takeLast(minOf(10, primitives.candles.size))
        val previous15Candles = primitives.candles.dropLast(minOf(10, primitives.candles.size)).takeLast(15)
        
        if (last10Candles.isEmpty() || last10Candles.size < 5) {
            state["volatilitySpikeDetected"] = true
            state["spikeIntensity"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "VolatilitySpikeGuard: Insufficient recent data (need >=5, got ${last10Candles.size})",
                weight = weight
            )
        }
        
        val recentRanges = last10Candles.map { abs(it.high - it.low) }.filter { it > 0.0 }
        
        if (recentRanges.isEmpty()) {
            state["volatilitySpikeDetected"] = true
            state["spikeIntensity"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "VolatilitySpikeGuard: No valid recent ranges - FAIL",
                weight = weight
            )
        }
        
        val previousRanges = if (previous15Candles.isNotEmpty()) {
            previous15Candles.map { abs(it.high - it.low) }.filter { it > 0.0 }
        } else {
            emptyList()
        }
        
        val recentAvgRange = recentRanges.average()
        
        val previousAvgRange = if (previousRanges.isNotEmpty()) {
            previousRanges.average()
        } else {
            atr
        }
        
        val spikeIntensity = if (previousAvgRange > 0.0) {
            (recentAvgRange / previousAvgRange - 1.0).coerceAtLeast(0.0)
        } else {
            0.0
        }
        
        val volatilitySpikeDetected = spikeIntensity > 0.5
        
        state["volatilitySpikeDetected"] = volatilitySpikeDetected
        state["spikeIntensity"] = spikeIntensity
        
        val passed = !volatilitySpikeDetected
        val confidence = (1.0 - spikeIntensity).coerceIn(0.0, 1.0)
        
        val reason = String.format(
            Locale.US,
            "VolatilitySpikeGuard: %.2f (recent=%.2f, prev=%.2f, spike=%.1f%%) - %s",
            spikeIntensity,
            recentAvgRange,
            previousAvgRange,
            spikeIntensity * 100,
            if (passed) "PASS" else "SPIKE_DETECTED"
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
