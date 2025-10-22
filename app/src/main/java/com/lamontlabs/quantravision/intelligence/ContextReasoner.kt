package com.lamontlabs.quantravision.intelligence

import com.lamontlabs.quantravision.PatternMatch
import kotlin.math.exp

/**
 * ContextReasoner — correlates multiple simultaneous detections to identify compound setups.
 * Example: Ascending Triangle + RSI Divergence -> Bullish Confluence.
 * Purely deterministic. No model weights. Runs post-detection.
 */
object ContextReasoner {

    data class ContextResult(
        val setupName: String,
        val confidence: Double,
        val components: List<String>
    )

    // Predefined deterministic combinations
    private val confluenceRules = listOf(
        listOf("Ascending Triangle", "RSI Bullish Divergence") to "Bullish Confluence",
        listOf("Descending Triangle", "RSI Bearish Divergence") to "Bearish Confluence",
        listOf("Double Bottom", "MACD Bullish Divergence") to "Reversal Confluence",
        listOf("Head & Shoulders", "Volume Drop") to "Top Confirmation",
        listOf("Inverse Head & Shoulders", "RSI Bullish Divergence") to "Bottom Confirmation"
    )

    fun correlate(matches: List<PatternMatch>): List<ContextResult> {
        val detectedNames = matches.map { it.patternName }
        val results = mutableListOf<ContextResult>()
        for ((rule, setup) in confluenceRules) {
            if (rule.all { detectedNames.contains(it) }) {
                val conf = deterministicConfidence(matches, rule)
                results.add(ContextResult(setup, conf, rule))
            }
        }
        return results
    }

    private fun deterministicConfidence(matches: List<PatternMatch>, rule: List<String>): Double {
        val vals = matches.filter { rule.contains(it.patternName) }.map { it.confidence }
        if (vals.isEmpty()) return 0.0
        val mean = vals.sum() / vals.size
        val spread = vals.max() - vals.min()
        val adjusted = mean * sigmoid(1.0 - spread)
        return String.format("%.4f", adjusted).toDouble()
    }

    private fun sigmoid(x: Double): Double {
        val e = exp(-x)
        return 1.0 / (1.0 + e)
    }
}
