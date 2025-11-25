package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class T35MarketCondition : ApexProtocol {
    override val protocolId = "T35"
    override val protocolName = "MarketCondition"
    override val weight = 2.3
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["marketConditionScore"] = 0.0
            state["marketRegime"] = "UNKNOWN"
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "MarketCondition: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val recentCandles = primitives.candles.takeLast(15)
        if (recentCandles.size < 2) {
            state["marketConditionScore"] = 0.0
            state["marketRegime"] = "UNKNOWN"
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "MarketCondition: Insufficient recent candles",
                weight = weight
            )
        }
        
        val firstPrice = recentCandles.first().close
        val lastPrice = recentCandles.last().close
        val trendStrength = if (firstPrice > 0.0) abs(lastPrice - firstPrice) / firstPrice else 0.0
        
        val prices = recentCandles.map { it.close }
        val mean = if (prices.isNotEmpty()) prices.average() else 0.0
        val variance = if (prices.isNotEmpty()) prices.map { (it - mean).pow(2) }.average() else 0.0
        val stdDev = sqrt(variance)
        val volatility = if (mean > 0.0) stdDev / mean else 0.0
        
        val marketRegime = when {
            trendStrength > 0.05 && volatility < 0.03 -> "TRENDING"
            volatility > 0.05 -> "VOLATILE"
            trendStrength < 0.02 -> "RANGING"
            else -> "MIXED"
        }
        
        val marketConditionScore = when (marketRegime) {
            "TRENDING" -> 0.85
            "RANGING" -> 0.65
            "MIXED" -> 0.50
            "VOLATILE" -> 0.35
            else -> 0.40
        }
        
        state["marketConditionScore"] = marketConditionScore
        state["marketRegime"] = marketRegime
        
        val passed = marketConditionScore >= 0.5
        val confidence = marketConditionScore
        
        val reason = String.format(
            Locale.US,
            "MarketCondition: %s (score=%.2f, trend=%.2f, volatility=%.2f) - %s",
            marketRegime,
            marketConditionScore,
            trendStrength,
            volatility,
            if (passed) "PASS" else "FAIL"
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
