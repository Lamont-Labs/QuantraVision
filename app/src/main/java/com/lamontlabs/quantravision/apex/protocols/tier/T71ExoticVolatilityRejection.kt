package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T71ExoticVolatilityRejection : ApexProtocol {
    override val protocolId = "T71"
    override val protocolName = "ExoticVolatilityRejection"
    override val weight = 3.2
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 35) {
            state["exoticVolatilityDetected"] = true
            state["exoticRejectionScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "ExoticVolatilityRejection: Insufficient candles (need >=35, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val volatilityException = state["volatilityException"] as? Boolean ?: false
        val volatilitySpikeDetected = state["volatilitySpikeDetected"] as? Boolean ?: false
        val abnormalMovementFlag = state["abnormalMovementFlag"] as? Boolean ?: false
        val volumeAnomalyDetected = state["volumeAnomalyDetected"] as? Boolean ?: false
        
        val recentCandles = primitives.candles.takeLast(15)
        var invertedPatternDetected = false
        var asymmetricSpikeDetected = false
        
        if (recentCandles.size >= 15) {
            val priceRanges = recentCandles.map { abs(it.high - it.low) }
            val volumes = recentCandles.map { it.volume }
            
            for (i in 0 until (recentCandles.size - 2)) {
                val priceMove = priceRanges[i]
                val volumeRatio = if (volumes[i] > 0) volumes[i + 1] / volumes[i] else 1.0
                
                if (priceMove > priceRanges.average() * 1.5 && volumeRatio < 0.5) {
                    invertedPatternDetected = true
                }
                
                if (priceMove < priceRanges.average() * 0.5 && volumeRatio > 2.0) {
                    asymmetricSpikeDetected = true
                }
            }
        }
        
        val exoticVolatilityDetected = invertedPatternDetected ||
                                       asymmetricSpikeDetected ||
                                       volatilityException ||
                                       abnormalMovementFlag
        
        val exoticRejectionScore = if (exoticVolatilityDetected) 0.0 else 1.0
        
        state["exoticVolatilityDetected"] = exoticVolatilityDetected
        state["exoticRejectionScore"] = exoticRejectionScore
        
        val passed = !exoticVolatilityDetected
        val confidence = exoticRejectionScore
        
        val reason = String.format(
            Locale.US,
            "ExoticVolatilityRejection: %s (inverted=%s, asymm=%s) - %s",
            if (exoticVolatilityDetected) "DETECTED" else "CLEAN",
            invertedPatternDetected,
            asymmetricSpikeDetected,
            if (passed) "PASS" else "REJECT"
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
