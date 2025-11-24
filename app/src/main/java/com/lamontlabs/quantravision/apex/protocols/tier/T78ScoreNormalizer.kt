package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T78ScoreNormalizer : ApexProtocol {
    override val protocolId = "T78"
    override val protocolName = "ScoreNormalizer"
    override val weight = 3.4
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["normalizedScore"] = 0.0
            state["scoreReady"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ScoreNormalizer: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val aggregatedConfidence = state["aggregatedConfidence"] as? Double ?: 0.0
        val crossLayerScore = state["crossLayerScore"] as? Double ?: 0.0
        val multiFrameContinuationScore = state["multiFrameContinuationScore"] as? Double ?: 0.0
        
        val normalizedScore = (aggregatedConfidence * 70.0) +
                             (crossLayerScore * 20.0) +
                             (multiFrameContinuationScore * 10.0)
        
        val scoreReady = normalizedScore >= 50.0
        
        state["normalizedScore"] = normalizedScore
        state["scoreReady"] = scoreReady
        
        val passed = scoreReady
        val confidence = normalizedScore / 100.0
        
        val reason = String.format(
            Locale.US,
            "ScoreNormalizer: %.1f (agg=%.2f, cross=%.2f, multi=%.2f) - %s",
            normalizedScore,
            aggregatedConfidence,
            crossLayerScore,
            multiFrameContinuationScore,
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
