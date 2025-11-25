package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T38ScaleInvariance : ApexProtocol {
    override val protocolId = "T38"
    override val protocolName = "ScaleInvariance"
    override val weight = 2.2
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 40) {
            state["scaleInvariantScore"] = 0.0
            state["scaleInvariant"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ScaleInvariance: Insufficient candles (need >=40, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val window10Trend = calculateWindowTrend(primitives.candles, 10)
        val window20Trend = calculateWindowTrend(primitives.candles, 20)
        val window40Trend = calculateWindowTrend(primitives.candles, 40)
        
        val trend10_20Match = window10Trend == window20Trend
        val trend20_40Match = window20Trend == window40Trend
        val allMatch = trend10_20Match && trend20_40Match
        
        val scaleInvariantScore = when {
            allMatch -> 0.90
            trend10_20Match || trend20_40Match -> 0.65
            else -> 0.35
        }
        
        state["scaleInvariantScore"] = scaleInvariantScore
        state["scaleInvariant"] = allMatch
        
        val passed = scaleInvariantScore >= 0.6
        val confidence = scaleInvariantScore
        
        val reason = String.format(
            Locale.US,
            "ScaleInvariance: %.2f - %s (w10=%s, w20=%s, w40=%s)",
            scaleInvariantScore,
            if (passed) "PASS" else "FAIL",
            window10Trend,
            window20Trend,
            window40Trend
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
    
    private fun calculateWindowTrend(candles: List<Candle>, windowSize: Int): String {
        val window = candles.takeLast(windowSize)
        if (window.isEmpty() || window.size < 2) return "UNKNOWN"
        val firstPrice = window.first().close
        val lastPrice = window.last().close
        return when {
            lastPrice > firstPrice * 1.02 -> "UP"
            lastPrice < firstPrice * 0.98 -> "DOWN"
            else -> "SIDEWAYS"
        }
    }
}
