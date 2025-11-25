package com.lamontlabs.quantravision.apex.protocols.learning

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class LP10DriftAdaptationAggregator : ApexProtocol {
    override val protocolId = "LP10"
    override val protocolName = "DriftAdaptationAggregator"
    override val weight = 1.30
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        // FAIL-CLOSED: Check candles
        if (primitives.candles.size < 10) {
            state["driftAdaptationScore"] = 0.0
            state["driftTrendVector"] = "UNKNOWN"
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "DriftAdaptationAggregator: Insufficient candles - FAIL (fail-closed)",
                weight = weight
            )
        }
        
        // FAIL-CLOSED: Verify required upstream drift state exists
        val requiredKeys = listOf("regimeShiftScore", "volatilityDriftMagnitude", "trendDriftScore")
        val missingKeys = requiredKeys.filter { !state.containsKey(it) }
        
        if (missingKeys.isNotEmpty()) {
            state["driftAdaptationScore"] = 0.0
            state["driftTrendVector"] = "MISSING_PREREQUISITES"
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = String.format(
                    Locale.US,
                    "DriftAdaptationAggregator: Missing prerequisites (%s) - FAIL (fail-closed)",
                    missingKeys.joinToString(", ")
                ),
                weight = weight
            )
        }
        
        // Read values (now guaranteed to exist)
        val regimeShiftScore = state["regimeShiftScore"] as? Double ?: 0.0
        val volatilityDrift = state["volatilityDriftMagnitude"] as? Double ?: 0.0
        val trendDrift = state["trendDriftScore"] as? Double ?: 0.0
        
        val aggregatedScore = (regimeShiftScore + (1.0 - volatilityDrift) + trendDrift) / 3.0
        val trendVector = deriveTrendVector(trendDrift, volatilityDrift)
        
        state["driftAdaptationScore"] = aggregatedScore
        state["driftTrendVector"] = trendVector
        
        val passed = aggregatedScore > 0.4
        val confidence = aggregatedScore
        
        val reason = String.format(
            Locale.US,
            "DriftAdaptationAggregator: Adaptation score %.3f, vector %s - %s",
            aggregatedScore,
            trendVector,
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
    
    private fun deriveTrendVector(trendDrift: Double, volatilityDrift: Double): String {
        return when {
            trendDrift > 0.6 && volatilityDrift < 0.5 -> "STABLE_UPTREND"
            trendDrift < 0.4 && volatilityDrift < 0.5 -> "STABLE_DOWNTREND"
            volatilityDrift > 0.7 -> "HIGH_VOLATILITY"
            else -> "NEUTRAL"
        }
    }
}
