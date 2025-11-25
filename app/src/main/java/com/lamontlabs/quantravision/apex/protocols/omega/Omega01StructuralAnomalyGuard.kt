package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*

class Omega01StructuralAnomalyGuard : ApexProtocol {
    override val protocolId = "Omega01"
    override val protocolName = "StructuralAnomalyGuard"
    override val weight = 5.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val anomalies = mutableListOf<String>()
        
        if (primitives.candles.size < 10) {
            state["omega01_passed"] = false
            state["omega01_anomalyCount"] = 1
            state["omega01_reason"] = "Insufficient candles (need >= 10, got ${primitives.candles.size})"
            
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "Omega01: FAIL - Insufficient candles (need >= 10, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        for ((index, candle) in primitives.candles.withIndex()) {
            if (candle.open <= 0.0) {
                anomalies.add("Candle $index: non-positive open price (${candle.open})")
            }
            if (candle.high <= 0.0) {
                anomalies.add("Candle $index: non-positive high price (${candle.high})")
            }
            if (candle.low <= 0.0) {
                anomalies.add("Candle $index: non-positive low price (${candle.low})")
            }
            if (candle.close <= 0.0) {
                anomalies.add("Candle $index: non-positive close price (${candle.close})")
            }
            
            if (candle.open.isNaN() || candle.open.isInfinite()) {
                anomalies.add("Candle $index: invalid open value (NaN/Infinity)")
            }
            if (candle.high.isNaN() || candle.high.isInfinite()) {
                anomalies.add("Candle $index: invalid high value (NaN/Infinity)")
            }
            if (candle.low.isNaN() || candle.low.isInfinite()) {
                anomalies.add("Candle $index: invalid low value (NaN/Infinity)")
            }
            if (candle.close.isNaN() || candle.close.isInfinite()) {
                anomalies.add("Candle $index: invalid close value (NaN/Infinity)")
            }
            
            if (candle.high < candle.low) {
                anomalies.add("Candle $index: high < low (${candle.high} < ${candle.low})")
            }
            if (candle.close < candle.low || candle.close > candle.high) {
                anomalies.add("Candle $index: close not in [low, high] range")
            }
            if (candle.open < candle.low || candle.open > candle.high) {
                anomalies.add("Candle $index: open not in [low, high] range")
            }
            
            if (candle.volume < 0.0) {
                anomalies.add("Candle $index: negative volume (${candle.volume})")
            }
        }
        
        for (i in 1 until primitives.candles.size) {
            val prevCandle = primitives.candles[i - 1]
            val currCandle = primitives.candles[i]
            
            if (currCandle.timestamp <= prevCandle.timestamp) {
                anomalies.add("Candles $i: non-monotonic timestamps (${prevCandle.timestamp} >= ${currCandle.timestamp})")
            }
            
            if (currCandle.timestamp == prevCandle.timestamp) {
                anomalies.add("Candles $i: duplicate timestamp (${currCandle.timestamp})")
            }
            
            val gap = currCandle.timestamp - prevCandle.timestamp
            val maxGap = 24 * 60 * 60 * 1000L
            if (gap > maxGap) {
                anomalies.add("Candles $i: excessive gap (${gap}ms > 24 hours)")
            }
        }
        
        val passed = anomalies.isEmpty()
        state["omega01_passed"] = passed
        state["omega01_anomalyCount"] = anomalies.size
        state["omega01_reason"] = if (passed) {
            "All structural checks passed"
        } else {
            anomalies.joinToString("; ")
        }
        
        val reason = if (passed) {
            "Omega01: PASS - All ${primitives.candles.size} candles structurally valid"
        } else {
            "Omega01: FAIL - ${anomalies.size} anomalies detected: ${anomalies.take(3).joinToString(", ")}"
        }
        
        return ProtocolVerdict(
            protocolId = protocolId,
            protocolName = protocolName,
            passed = passed,
            confidence = if (passed) 1.0 else 0.0,
            reason = reason,
            weight = weight
        )
    }
}
