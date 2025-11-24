package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.abs
import kotlin.math.max
/**
 * T06: VolatilityAssessment
 * Purpose: Calculates and validates volatility metrics
 * Category: Structural Quality
 * 
 * BATCH 8 FIX: Uses actual candle data, fully deterministic
 */
class T06VolatilityAssessment : ApexProtocol {
    override val protocolId = "T06"
    override val protocolName = "VolatilityAssessment"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 2) {
            state["volatility"] = "unknown"
            state["atr"] = 0.0
            state["volatilityPercentile"] = 50.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.7,
                reason = "Volatility: insufficient data",
                weight = weight
            )
        }
        
        // Calculate ATR (Average True Range)
        val trueRanges = mutableListOf<Double>()
        for (i in 1 until primitives.candles.size) {
            val candle = primitives.candles[i]
            val prevCandle = primitives.candles[i - 1]
            val high = candle.high
            val low = candle.low
            val prevClose = prevCandle.close
            
            val tr = max(
                high - low,
                max(abs(high - prevClose), abs(low - prevClose))
            trueRanges.add(tr)
        val atr = if (trueRanges.isNotEmpty()) trueRanges.average() else 0.0
        val avgPrice = primitives.candles.map { it.close }.average()
        val atrPercent = (atr / avgPrice) * 100.0
        // Classify volatility
        val volatilityState = when {
            atrPercent < 0.5 -> "low"
            atrPercent < 2.0 -> "normal"
            atrPercent < 5.0 -> "high"
            else -> "extreme"
        val volatilityPercentile = when (volatilityState) {
            "low" -> 25.0
            "normal" -> 50.0
            "high" -> 75.0
            "extreme" -> 95.0
            else -> 50.0
        state["volatility"] = volatilityState
        state["atr"] = atr
        state["volatilityPercentile"] = volatilityPercentile
        val (passed, confidence) = when (volatilityState) {
            "extreme" -> Pair(false, 0.3)
            "normal" -> Pair(true, 1.0)
            else -> Pair(true, 0.7)
        val reason = "Volatility: $volatilityState, ATR: ${"%.2f".format(Locale.US, atr)} (${"%.2f".format(Locale.US, atrPercent)}%)"
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
