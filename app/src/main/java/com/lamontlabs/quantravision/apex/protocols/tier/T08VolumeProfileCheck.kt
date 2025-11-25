package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.max
import kotlin.math.min
/**
 * T08: VolumeProfileCheck
 * Purpose: Validates volume distribution and anomalies
 * Category: Structural Quality
 * 
 * BATCH 8 FIX: Uses actual candle volume data, fully deterministic
 */
class T08VolumeProfileCheck : ApexProtocol {
    override val protocolId = "T08"
    override val protocolName = "VolumeProfileCheck"
    override val weight = 1.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val volumes = primitives.candles.map { it.volume }
        
        if (volumes.isEmpty() || volumes.all { it == 0.0 }) {
            state["volumeQuality"] = 0.5
            state["avgVolume"] = 0.0
            state["volumeSpikes"] = 0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Volume quality: 0.50 - no volume data",
                weight = weight
            )
        }
        val avgVolume = volumes.average()
        // Check for extreme spikes
        val spikes = volumes.count { it > avgVolume * 3.0 }
        val spikeRatio = spikes.toDouble() / volumes.size.toDouble()
        // Calculate volume consistency
        val mean = avgVolume
        val variance = volumes.map { (it - mean) * (it - mean) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        val cv = if (avgVolume > 0) stdDev / avgVolume else 1.0
        val consistency = max(0.0, 1.0 - min(1.0, cv / 2.0))
        // Volume quality score
        val volumeQuality = max(0.0, min(1.0, consistency * (1.0 - spikeRatio * 0.5)))
        state["volumeQuality"] = volumeQuality
        state["avgVolume"] = avgVolume
        state["volumeSpikes"] = spikes
        val passed = volumeQuality >= 0.4
        val confidence = volumeQuality
        val profile = if (spikeRatio > 0.2) "volatile" else "stable"
        val reason = "Volume quality: ${"%.2f".format(Locale.US, volumeQuality)} - $profile"
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
