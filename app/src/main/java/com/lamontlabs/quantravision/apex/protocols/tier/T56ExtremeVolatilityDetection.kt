package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T56ExtremeVolatilityDetection : ApexProtocol {
    override val protocolId = "T56"
    override val protocolName = "ExtremeVolatilityDetection"
    override val weight = 2.8
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val validCandles = primitives.candles.filter { it.close > 0.0 }
        
        if (validCandles.size < 30) {
            state["volatilityException"] = true
            state["volatilityExceptionScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ExtremeVolatilityDetection: Insufficient valid candles (need >=30, got ${validCandles.size})",
                weight = weight
            )
        }
        
        val upstreamVolatility = state["volatility"] as? Double
        val upstreamATR = state["atr"] as? Double
        
        if (upstreamVolatility == null || upstreamATR == null) {
            state["volatilityException"] = true
            state["volatilityExceptionScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ExtremeVolatilityDetection: Missing upstream volatility/ATR data - FAIL (fail-closed)",
                weight = weight
            )
        }
        
        val volatility = upstreamVolatility
        val atr = upstreamATR
        
        val recentCandles = validCandles.takeLast(minOf(20, validCandles.size))
        
        if (recentCandles.isEmpty()) {
            state["volatilityException"] = true
            state["volatilityExceptionScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ExtremeVolatilityDetection: Empty recent candles - FAIL",
                weight = weight
            )
        }
        
        val allVolatilities = validCandles.map { candle ->
            (candle.high - candle.low) / candle.close
        }.filter { it > 0.0 }
        
        if (allVolatilities.isEmpty()) {
            state["volatilityException"] = true
            state["volatilityExceptionScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ExtremeVolatilityDetection: No valid volatilities - FAIL",
                weight = weight
            )
        }
        
        val medianVolatility = run {
            val sortedVolatilities = allVolatilities.sorted()
            sortedVolatilities[sortedVolatilities.size / 2]
        }
        
        val recentRanges = recentCandles.map { abs(it.high - it.low) }.filter { it > 0.0 }
        
        if (recentRanges.isEmpty()) {
            state["volatilityException"] = true
            state["volatilityExceptionScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ExtremeVolatilityDetection: No valid recent ranges - FAIL",
                weight = weight
            )
        }
        
        val atrRecent = recentRanges.average()
        
        val volatilityRatio = if (medianVolatility > 0.0) {
            volatility / medianVolatility
        } else {
            1.0
        }
        
        val atrSpikeRatio = if (atr > 0.0) {
            atrRecent / atr
        } else {
            1.0
        }
        
        val volatilityException = volatilityRatio > 3.0 || atrSpikeRatio > 2.0
        
        val volatilityExceptionScore = when {
            volatilityRatio > 5.0 || atrSpikeRatio > 3.0 -> 0.95
            volatilityRatio > 4.0 || atrSpikeRatio > 2.5 -> 0.85
            volatilityRatio > 3.0 || atrSpikeRatio > 2.0 -> 0.70
            volatilityRatio > 2.0 || atrSpikeRatio > 1.5 -> 0.50
            else -> 0.25
        }
        
        state["volatilityException"] = volatilityException
        state["volatilityExceptionScore"] = volatilityExceptionScore
        
        val passed = !volatilityException
        val confidence = 1.0 - volatilityExceptionScore
        
        val reason = String.format(
            Locale.US,
            "ExtremeVolatilityDetection: %.2f (volRatio=%.2fx, atrRatio=%.2fx) - %s",
            volatilityExceptionScore,
            volatilityRatio,
            atrSpikeRatio,
            if (passed) "PASS" else "EXCEPTION"
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
