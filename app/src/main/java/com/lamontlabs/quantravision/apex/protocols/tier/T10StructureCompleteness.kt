package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.max
/**
 * T10: StructureCompleteness
 * Purpose: Validates pattern structure has required elements
 * Category: Structural Quality
 * 
 * BATCH 8 FIX: Uses actual candle and line data, fully deterministic
 */
class T10StructureCompleteness : ApexProtocol {
    override val protocolId = "T10"
    override val protocolName = "StructureCompleteness"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val requiredElements = mutableListOf<String>()
        val missingElements = mutableListOf<String>()
        
        requiredElements.add("swingPoints")
        requiredElements.add("trendlines")
        requiredElements.add("priceAction")
        // Check swing points (minimum 3 for pattern formation)
        val prices = primitives.candles.map { it.close }
        var swingPoints = 0
        for (i in 1 until prices.size - 1) {
            val isPeak = prices[i] > prices[i - 1] && prices[i] > prices[i + 1]
            val isTrough = prices[i] < prices[i - 1] && prices[i] < prices[i + 1]
            if (isPeak || isTrough) swingPoints++
        }
        if (swingPoints < 3) {
            missingElements.add("swingPoints($swingPoints/3)")
        }
        // Check trendlines
        if (primitives.detectedLines.isEmpty()) {
            missingElements.add("trendlines")
        }
        // Check price action data
        if (primitives.candles.size < 10) {
            missingElements.add("priceAction")
        }
        val structureComplete = missingElements.isEmpty()
        state["structureComplete"] = structureComplete
        state["swingPointCount"] = swingPoints
        state["requiredElements"] = requiredElements
        state["missingElements"] = missingElements
        val passed = structureComplete
        val confidence = if (passed) 1.0 else max(0.0, 1.0 - missingElements.size * 0.3)
        val elementsSummary = if (passed) {
            "swings=$swingPoints, lines=${primitives.detectedLines.size}, candles=${primitives.candles.size}"
        } else {
            "missing: ${missingElements.joinToString(", ")}"
        }
        val reason = if (passed) {
            "Structure: COMPLETE - $elementsSummary"
        } else {
            "Structure: INCOMPLETE - $elementsSummary"
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
