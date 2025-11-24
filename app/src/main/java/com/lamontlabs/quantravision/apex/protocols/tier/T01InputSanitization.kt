package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
/**
 * T01: InputSanitization
 * Purpose: Validates ChartPrimitives are non-null, within valid ranges
 * Category: Input Validation & Sanitization
 * 
 * BATCH 8 FIX: Now uses actual ChartPrimitives.candles field,
 * NO hash-based pseudo-random, deterministic validation only.
 */
class T01InputSanitization : ApexProtocol {
    override val protocolId = "T01"
    override val protocolName = "InputSanitization"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val issues = mutableListOf<String>()
        
        // Check ticker is non-empty
        if (context.ticker.isNullOrBlank()) {
            issues.add("ticker missing")
        }
        // Check timeframe is valid
        if (context.timeframe.isNullOrBlank()) {
            issues.add("timeframe missing")
        }
        // Check primitives hash exists
        if (primitives.rawImageHash.isBlank()) {
            issues.add("image hash missing")
        }
        // Check for minimum candle data (use actual candles field)
        val candleCount = primitives.candles.size
        if (candleCount < 10) {
            issues.add("insufficient candles (need >=10, got $candleCount)")
        }
        val inputValid = issues.isEmpty()
        state["inputValid"] = inputValid
        state["ticker"] = context.ticker ?: ""
        state["timeframe"] = context.timeframe ?: ""
        state["candleCount"] = candleCount
        val passed = inputValid
        val confidence = if (passed) 1.0 else 0.0
        val scoreImpact = if (passed) 0.0 else -100.0
        val reason = if (passed) {
            "Input validation: PASS"
        } else {
            "Input validation: FAIL - ${issues.joinToString(", ")}"
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
