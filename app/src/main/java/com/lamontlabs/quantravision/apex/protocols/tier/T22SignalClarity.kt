package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T22SignalClarity : ApexProtocol {
    override val protocolId = "T22"
    override val protocolName = "SignalClarity"
    override val weight = 1.7
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 10) {
            state["signalClarityScore"] = 0.0
            state["signalClarityOk"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "SignalClarity: Insufficient candles (need >=10, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val passChecks = listOf(
            state["inputValid"] as? Boolean ?: false,
            state["geometryValid"] as? Boolean ?: false,
            state["ohlcValid"] as? Boolean ?: false,
            state["timeframeConsistent"] as? Boolean ?: false,
            state["priceNormalized"] as? Boolean ?: false,
            (state["volatility"] as? Double ?: 0.0) > 0.0,
            (state["trendStrength"] as? Double ?: 0.0) >= 0.3,
            state["volumeProfile"] != null,
            state["supportLevels"] != null,
            state["structureComplete"] as? Boolean ?: false,
            state["momentumAligned"] as? Boolean ?: false,
            state["volumeConfirmed"] as? Boolean ?: false,
            state["volatilityAligned"] as? Boolean ?: false,
            state["priceActionQuality"] != null,
            state["mtfCoherent"] as? Boolean ?: false,
            state["entropyEarly"] as? Boolean ?: false,
            (state["conflictCount"] as? Int ?: 0) <= 3,
            state["regimeMatch"] as? Boolean ?: false,
            state["signalClarity"] as? Boolean ?: false,
            state["finalEntropyOk"] as? Boolean ?: false
        )
        
        val passedCount = passChecks.count { it }
        val totalProtocols = passChecks.size
        val signalClarityScore = if (totalProtocols > 0) passedCount.toDouble() / totalProtocols else 0.0
        
        state["signalClarityScore"] = signalClarityScore
        state["signalClarityOk"] = signalClarityScore >= 0.6
        
        val passed = signalClarityScore >= 0.6
        val confidence = signalClarityScore
        
        val reason = String.format(
            Locale.US,
            "SignalClarity: %.2f (threshold 0.6) - %s (%d/%d protocols passed)",
            signalClarityScore,
            if (passed) "PASS" else "FAIL",
            passedCount,
            totalProtocols
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
