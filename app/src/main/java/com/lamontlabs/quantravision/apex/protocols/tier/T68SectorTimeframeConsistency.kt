package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T68SectorTimeframeConsistency : ApexProtocol {
    override val protocolId = "T68"
    override val protocolName = "SectorTimeframeConsistency"
    override val weight = 3.2
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["sectorTimeframeConsistent"] = false
            state["consistencyLevel"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "SectorTimeframeConsistency: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val sectorTrendValid = state["sectorTrendValid"] as? Boolean ?: false
        val multiFrameFused = state["multiFrameFused"] as? Boolean ?: false
        val regimeStable = state["regimeStable"] as? Boolean ?: false
        val temporalCoherence = state["temporalCoherence"] as? Double ?: 0.0
        
        val trueCount = listOf(
            sectorTrendValid,
            multiFrameFused,
            regimeStable,
            temporalCoherence >= 0.65
        ).count { it }
        
        val consistencyLevel = trueCount / 4.0
        val sectorTimeframeConsistent = consistencyLevel >= 0.75
        
        state["sectorTimeframeConsistent"] = sectorTimeframeConsistent
        state["consistencyLevel"] = consistencyLevel
        
        val passed = consistencyLevel >= 0.75
        val confidence = consistencyLevel
        
        val reason = String.format(
            Locale.US,
            "SectorTimeframeConsistency: %.2f (%d/4 consistent) - %s",
            consistencyLevel,
            trueCount,
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
