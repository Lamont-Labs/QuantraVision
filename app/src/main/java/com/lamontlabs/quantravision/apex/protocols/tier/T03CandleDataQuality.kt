package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
/**
 * T03: CandleDataQuality
 * Purpose: Validates OHLC integrity (Open/High/Low/Close relationships)
 * Category: Input Validation & Sanitization
 * 
 * BATCH 8 FIX: Uses actual candle OHLC data, fully deterministic
 */
class T03CandleDataQuality : ApexProtocol {
    override val protocolId = "T03"
    override val protocolName = "CandleDataQuality"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.isEmpty()) {
            state["candleQualityScore"] = 0.0
            state["ohlcValid"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "Candle quality: no data",
                weight = weight
            )
        }
        
        var qualityScore = 1.0
        var ohlcValid = true
        val issues = mutableListOf<String>()
        for ((index, candle) in primitives.candles.withIndex()) {
            // Check: High >= Low
            if (candle.high < candle.low) {
                issues.add("candle $index: high < low")
                qualityScore -= 0.05
                ohlcValid = false
            }
            
            // Check: Close within [Low, High]
            if (candle.close < candle.low || candle.close > candle.high) {
                issues.add("candle $index: close out of range")
            }
            // Check: Open within [Low, High]
            if (candle.open < candle.low || candle.open > candle.high) {
                issues.add("candle $index: open out of range")
            }
            // Check: No negative prices
            if (candle.open <= 0.0 || candle.high <= 0.0 || candle.low <= 0.0 || candle.close <= 0.0) {
                issues.add("candle $index: invalid price (<= 0)")
            }
        }
        qualityScore = qualityScore.coerceIn(0.0, 1.0)
        state["candleQualityScore"] = qualityScore
        state["ohlcValid"] = ohlcValid
        val passed = qualityScore >= 0.5
        val confidence = qualityScore
        val reason = if (qualityScore >= 0.9) {
            "Candle quality: ${"%.2f".format(Locale.US, qualityScore)} - excellent"
        } else if (qualityScore >= 0.5) {
            "Candle quality: ${"%.2f".format(Locale.US, qualityScore)} - acceptable"
        } else {
            "Candle quality: ${"%.2f".format(Locale.US, qualityScore)} - poor (${issues.take(3).joinToString(", ")})"
        }
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
