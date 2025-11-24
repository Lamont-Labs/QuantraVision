package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.min
/**
 * T05: PriceRangeNormalization
 * Purpose: Validates price range is reasonable, normalizes for analysis
 * Category: Input Validation & Sanitization
 * 
 * BATCH 8 FIX: Uses actual candle prices, fully deterministic
 */
class T05PriceRangeNormalization : ApexProtocol {
    override val protocolId = "T05"
    override val protocolName = "PriceRangeNormalization"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.isEmpty()) {
            state["priceRangeValid"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "Price range: FAIL - no price data",
                weight = weight
            )
        }
        
        val prices = primitives.candles.flatMap { listOf(it.low, it.high) }
        val minPrice = prices.minOrNull() ?: 0.0
        val maxPrice = prices.maxOrNull() ?: 0.0
        val avgPrice = prices.average()
        val priceRange = maxPrice - minPrice
        val rangePercent = if (avgPrice > 0) (priceRange / avgPrice) * 100.0 else 0.0
        val issues = mutableListOf<String>()
        // Check: Price range > 0.01%
        if (rangePercent < 0.01) {
            issues.add("range too small (${"%.3f".format(Locale.US, rangePercent)}%)")
        }
        // Check: Price range < 500% of average
        if (rangePercent > 500.0) {
            issues.add("range too large (${"%.1f".format(Locale.US, rangePercent)}%)")
        }
        // Normalize to 0.0-1.0
        val normalizedRange = if (priceRange > 0) {
            min(1.0, rangePercent / 100.0)
        } else {
            0.0
        }
        val priceRangeValid = issues.isEmpty()
        state["priceRangeValid"] = priceRangeValid
        state["normalizedRange"] = normalizedRange
        state["minPrice"] = minPrice
        state["maxPrice"] = maxPrice
        val passed = priceRangeValid
        val confidence = if (passed) 1.0 else 0.0
        val reason = if (passed) {
            "Price range: ${"%.2f".format(Locale.US, minPrice)}-${"%.2f".format(Locale.US, maxPrice)}, normalized: ${"%.3f".format(Locale.US, normalizedRange)}"
        } else {
            "Price range: FAIL - ${issues.joinToString(", ")}"
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
