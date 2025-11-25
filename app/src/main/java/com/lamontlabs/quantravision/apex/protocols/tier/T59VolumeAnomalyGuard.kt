package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T59VolumeAnomalyGuard : ApexProtocol {
    override val protocolId = "T59"
    override val protocolName = "VolumeAnomalyGuard"
    override val weight = 3.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["volumeAnomalyDetected"] = true
            state["volumeAnomalyScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "VolumeAnomalyGuard: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val volumes = primitives.candles.map { it.volume }
        
        if (volumes.isEmpty()) {
            state["volumeAnomalyDetected"] = false
            state["volumeAnomalyScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 1.0,
                reason = "VolumeAnomalyGuard: No volume data - PASS",
                weight = weight
            )
        }
        
        val sortedVolumes = volumes.sorted()
        val medianVolume = sortedVolumes[sortedVolumes.size / 2]
        
        val recentWindow = volumes.takeLast(minOf(10, volumes.size))
        val maxRecentVolume = if (recentWindow.isNotEmpty()) {
            recentWindow.maxOrNull() ?: 0.0
        } else {
            0.0
        }
        
        val volumeRatio = if (medianVolume > 0.0) {
            maxRecentVolume / medianVolume
        } else {
            1.0
        }
        
        val volumeAnomalyDetected = volumeRatio > 5.0
        
        val volumeAnomalyScore = when {
            volumeRatio > 10.0 -> 0.95
            volumeRatio > 7.5 -> 0.85
            volumeRatio > 5.0 -> 0.70
            volumeRatio > 3.0 -> 0.45
            else -> 0.20
        }
        
        state["volumeAnomalyDetected"] = volumeAnomalyDetected
        state["volumeAnomalyScore"] = volumeAnomalyScore
        
        val passed = !volumeAnomalyDetected
        val confidence = 1.0 - volumeAnomalyScore
        
        val reason = String.format(
            Locale.US,
            "VolumeAnomalyGuard: %.2f (ratio=%.1fx, median=%.0f, max=%.0f) - %s",
            volumeAnomalyScore,
            volumeRatio,
            medianVolume,
            maxRecentVolume,
            if (passed) "PASS" else "ANOMALY"
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
