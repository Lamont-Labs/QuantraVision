package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
/**
 * T16: EntropyGateEarly
 * Purpose: Early entropy detection to fail fast on noisy charts
 * Category: Entropy & Conflict Detection
 * 
 * BATCH 8 FIX: Uses actual candle data, NO hashCode(), fully deterministic
 */
class T16EntropyGateEarly : ApexProtocol {
    override val protocolId = "T16"
    override val protocolName = "EntropyGateEarly"
    override val weight = 2.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 5) {
            state["entropyScore"] = 0.5
            state["entropyStatus"] = "UNKNOWN"
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Entropy: 0.50 - insufficient data",
                weight = weight
            )
        }
        
        val prices = primitives.candles.map { it.close }
        // Calculate entropy from price distribution
        val entropyScore = calculateEntropy(prices)
        // Safe state access - check for conflicts
        val hasConflicts = checkConflicts(state)
        // Adjust entropy based on conflicts
        val adjustedEntropy = if (hasConflicts) {
            min(1.0, entropyScore * 1.3)
        } else {
            entropyScore
        }
        // Classify entropy level
        val (status, passed, confidence) = when {
            adjustedEntropy > 0.60 -> Triple("FAIL", false, 0.2)
            adjustedEntropy > 0.40 -> Triple("WARN", true, 0.5)
            else -> Triple("PASS", true, 1.0 - adjustedEntropy)
        }
        state["entropyScore"] = adjustedEntropy
        state["entropyStatus"] = status
        val reason = "Entropy: ${"%.2f".format(Locale.US, adjustedEntropy)} - $status"
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = passed,
            confidence = confidence,
            reason = reason,
            weight = weight
        )
    }
    private fun calculateEntropy(prices: List<Double>): Double {
        if (prices.size < 5) return 1.0
        // Calculate returns
        val returns = prices.zipWithNext { a, b -> (b - a) / a }
        // Normalize to probability distribution
        val minReturn = returns.minOrNull() ?: 0.0
        val maxReturn = returns.maxOrNull() ?: 0.0
        val range = maxReturn - minReturn
        if (range < 0.001) return 0.0
        // Create histogram bins
        val bins = 10
        val histogram = IntArray(bins)
        for (ret in returns) {
            val normalized = (ret - minReturn) / range
            val binIndex = (normalized * (bins - 1)).toInt().coerceIn(0, bins - 1)
            histogram[binIndex]++
        }
        // Calculate Shannon entropy
        var entropy = 0.0
        val total = returns.size.toDouble()
        for (count in histogram) {
            if (count > 0) {
                val p = count / total
                entropy -= p * ln(p)
            }
        }
        // Normalize to 0-1 (max entropy for 10 bins is ln(10) ≈ 2.3)
        return min(1.0, entropy / 2.3)
    }
    private fun checkConflicts(state: Map<String, Any>): Boolean {
        // Safe state access - check if previous protocols detected conflicts
        val momentumAligned = state["momentumAligned"] as? Boolean ?: true
        val volumeConfirmed = state["volumeConfirmed"] as? Boolean ?: true
        val volatilityAligned = state["volatilityAligned"] as? Boolean ?: true
        return !momentumAligned || !volumeConfirmed || !volatilityAligned
    }
}
