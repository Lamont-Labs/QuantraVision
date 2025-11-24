package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
/**
 * T02: ChartGeometryValidation
 * Purpose: Validates chart dimensions, aspect ratio, coordinate systems
 * Category: Input Validation & Sanitization
 * 
 * BATCH 8 FIX: Minimal validation since primitives don't include width/height.
 * Real geometry validation will be added when vision models provide actual dimensions.
 * For now, we validate that candles exist and are properly structured.
 */
class T02ChartGeometryValidation : ApexProtocol {
    override val protocolId = "T02"
    override val protocolName = "ChartGeometryValidation"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        // Validate that candles exist and are non-empty
        val hasCandles = primitives.candles.isNotEmpty()
        
        // Validate candle data integrity if present
        val candlesValid = if (hasCandles) {
            primitives.candles.all { candle ->
                candle.high >= candle.low &&
                candle.high >= candle.open &&
                candle.high >= candle.close &&
                candle.low <= candle.open &&
                candle.low <= candle.close
            }
        } else {
            false
        }
        val isValid = hasCandles && candlesValid
        // Set state - no placeholder geometry values
        state["geometryValid"] = isValid
        state["candleCount"] = primitives.candles.size
        state["candlesValid"] = candlesValid
        val passed = isValid
        val confidence = if (passed) 1.0 else 0.0
        val reason = when {
            !hasCandles -> "Geometry validation: FAIL - no candle data"
            !candlesValid -> "Geometry validation: FAIL - invalid candle OHLC relationships"
            else -> "Geometry validation: PASS (${primitives.candles.size} candles)"
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
