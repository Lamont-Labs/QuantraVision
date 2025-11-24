package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T30BreakoutValidation : ApexProtocol {
    override val protocolId = "T30"
    override val protocolName = "BreakoutValidation"
    override val weight = 2.1
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["breakoutCandidate"] = false
            state["breakoutScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "BreakoutValidation: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val priceRange = state["priceRange"] as? Double ?: 0.0
        
        val volumes = primitives.candles.map { it.volume }
        val avgVolume = if (volumes.isNotEmpty()) volumes.average() else 0.0
        
        val recentVolumes = primitives.candles.takeLast(5).map { it.volume }
        val recentVolume = if (recentVolumes.isNotEmpty()) recentVolumes.average() else 0.0
        
        val highestPrice = primitives.candles.maxOfOrNull { it.high } ?: 0.0
        val lowestPrice = primitives.candles.minOfOrNull { it.low } ?: 0.0
        val currentPrice = primitives.candles.last().close
        
        val pricePosition = if (highestPrice > lowestPrice && (highestPrice - lowestPrice) > 0.0) {
            (currentPrice - lowestPrice) / (highestPrice - lowestPrice)
        } else 0.5
        
        val volumeSpike = if (avgVolume > 0.0) recentVolume / avgVolume else 1.0
        
        val nearExtreme = pricePosition > 0.85 || pricePosition < 0.15
        val volumeConfirmsBreakout = volumeSpike > 1.3
        
        val breakoutScore = when {
            nearExtreme && volumeConfirmsBreakout -> 0.85
            nearExtreme -> 0.65
            volumeConfirmsBreakout -> 0.55
            else -> 0.30
        }
        
        val breakoutCandidate = nearExtreme && volumeConfirmsBreakout
        
        state["breakoutCandidate"] = breakoutCandidate
        state["breakoutScore"] = breakoutScore
        
        val passed = breakoutScore >= 0.6
        val confidence = breakoutScore
        
        val reason = String.format(
            Locale.US,
            "BreakoutValidation: %.2f - %s (position=%.2f, volSpike=%.2f, candidate=%s)",
            breakoutScore,
            if (passed) "PASS" else "FAIL",
            pricePosition,
            volumeSpike,
            breakoutCandidate
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
