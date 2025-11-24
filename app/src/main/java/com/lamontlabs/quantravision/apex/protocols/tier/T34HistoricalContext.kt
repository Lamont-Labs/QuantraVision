package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T34HistoricalContext : ApexProtocol {
    override val protocolId = "T34"
    override val protocolName = "HistoricalContext"
    override val weight = 2.2
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["historicalContextScore"] = 0.0
            state["percentileRank"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "HistoricalContext: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val currentPrice = primitives.candles.last().close
        val sortedPrices = primitives.candles.map { it.close }.sorted()
        
        val rank = sortedPrices.count { it <= currentPrice }
        val percentileRank = if (sortedPrices.isNotEmpty()) rank.toDouble() / sortedPrices.size else 0.5
        
        val midRange = percentileRank in 0.3..0.7
        val historicalContextScore = if (midRange) 0.75 else if (percentileRank in 0.2..0.8) 0.6 else 0.4
        
        state["historicalContextScore"] = historicalContextScore
        state["percentileRank"] = percentileRank
        
        val passed = historicalContextScore >= 0.5
        val confidence = historicalContextScore
        
        val reason = String.format(
            Locale.US,
            "HistoricalContext: score=%.2f, percentile=%.2f - %s",
            historicalContextScore,
            percentileRank,
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
