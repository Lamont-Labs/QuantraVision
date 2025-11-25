package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T36TimeframeAlignment : ApexProtocol {
    override val protocolId = "T36"
    override val protocolName = "TimeframeAlignment"
    override val weight = 2.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["timeframeAlignmentScore"] = 0.0
            state["multiTFAligned"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "TimeframeAlignment: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val originalTrend = calculateTrend(primitives.candles)
        
        val higherTFCandles = primitives.candles.filterIndexed { index, _ -> index % 3 == 0 }
        if (higherTFCandles.size < 10) {
            state["timeframeAlignmentScore"] = 0.0
            state["multiTFAligned"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "TimeframeAlignment: Insufficient stride data (need >=10, got ${higherTFCandles.size})",
                weight = weight
            )
        }
        
        val higherTFTrend = calculateTrend(higherTFCandles)
        
        val trendsAlign = originalTrend == higherTFTrend
        val timeframeAlignmentScore = if (trendsAlign) 0.85 else 0.45
        
        state["timeframeAlignmentScore"] = timeframeAlignmentScore
        state["multiTFAligned"] = trendsAlign
        
        val passed = trendsAlign
        val confidence = timeframeAlignmentScore
        
        val reason = String.format(
            Locale.US,
            "TimeframeAlignment: %s vs %s - %s (score=%.2f)",
            originalTrend,
            higherTFTrend,
            if (passed) "PASS" else "FAIL",
            timeframeAlignmentScore
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
    
    private fun calculateTrend(candles: List<Candle>): String {
        if (candles.size < 2) return "UNKNOWN"
        val firstPrice = candles.first().close
        val lastPrice = candles.last().close
        return when {
            lastPrice > firstPrice * 1.02 -> "UP"
            lastPrice < firstPrice * 0.98 -> "DOWN"
            else -> "SIDEWAYS"
        }
    }
}
