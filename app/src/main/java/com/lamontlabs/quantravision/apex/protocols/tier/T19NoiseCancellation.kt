package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
/**
 * T19: NoiseCancellation
 * Purpose: Filters out noise and false signals
 * Category: Entropy & Conflict Detection
 * 
 * BATCH 8 FIX: Uses actual candle data, NO hashCode(), fully deterministic
 */
class T19NoiseCancellation : ApexProtocol {
    override val protocolId = "T19"
    override val protocolName = "NoiseCancellation"
    override val weight = 1.3
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 5) {
            state["signalClarity"] = 0.5
            state["signalStrength"] = 0.0
            state["noiseLevel"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Signal clarity: 0.50 - insufficient data",
                weight = weight
            )
        }
        
        val prices = primitives.candles.map { it.close }
        // Calculate signal strength (trend consistency)
        val signalStrength = calculateSignalStrength(prices)
        // Calculate noise level (random fluctuations)
        val noiseLevel = calculateNoiseLevel(prices)
        // Signal-to-noise ratio
        val snr = if (noiseLevel > 0) signalStrength / noiseLevel else signalStrength
        // Normalize to 0-1 (clarity score)
        val signalClarity = min(1.0, max(0.0, snr / 3.0))
        state["signalClarity"] = signalClarity
        state["signalStrength"] = signalStrength
        state["noiseLevel"] = noiseLevel
        val passed = signalClarity >= 0.3
        val confidence = signalClarity
        val reason = "Signal clarity: ${"%.2f".format(Locale.US, signalClarity)} (SNR: ${"%.2f".format(Locale.US, snr)})"
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = passed,
            confidence = confidence,
            reason = reason,
            weight = weight
        )
    }
    private fun calculateSignalStrength(prices: List<Double>): Double {
        if (prices.size < 5) return 0.0
        // Use moving average as signal
        val windowSize = 5
        val smoothedPrices = mutableListOf<Double>()
        for (i in 0..(prices.size - windowSize)) {
            val avg = prices.subList(i, i + windowSize).average()
            smoothedPrices.add(avg)
        }
        // Calculate trend strength of smoothed signal
        if (smoothedPrices.size < 2) return 0.0
        val changes = smoothedPrices.zipWithNext { a, b -> abs(b - a) }
        return changes.average() * 100.0
    }
    private fun calculateNoiseLevel(prices: List<Double>): Double {
        // Use high-frequency fluctuations as noise
        val highFreqChanges = mutableListOf<Double>()
        for (i in 0 until prices.size - 1) {
            val change = abs(prices[i + 1] - prices[i])
            highFreqChanges.add(change)
        }
        // Calculate standard deviation of changes (noise measure)
        val mean = highFreqChanges.average()
        val variance = highFreqChanges.map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance) * 100.0
    }
}
