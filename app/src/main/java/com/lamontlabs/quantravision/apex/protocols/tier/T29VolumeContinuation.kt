package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T29VolumeContinuation : ApexProtocol {
    override val protocolId = "T29"
    override val protocolName = "VolumeContinuation"
    override val weight = 2.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 15) {
            state["volumeCarry"] = 0.0
            state["volumePersisting"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "VolumeContinuation: Insufficient candles (need >=15, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val volumeConfirmed = state["volumeConfirmed"] as? Boolean ?: false
        val volumeConfirmationScore = state["volumeConfirmationScore"] as? Double ?: 0.5
        
        val volumes = primitives.candles.map { it.volume }
        val avgVolume = if (volumes.isNotEmpty()) volumes.average() else 0.0
        
        val recentVolumes = primitives.candles.takeLast(5).map { it.volume }
        val recentVolume = if (recentVolumes.isNotEmpty()) recentVolumes.average() else 0.0
        
        val volumeRatio = if (avgVolume > 0.0) recentVolume / avgVolume else 1.0
        val volumeMaintained = volumeRatio >= 0.8
        
        val volumeCarry = volumeConfirmationScore * if (volumeMaintained) 0.95 else 0.6
        val volumePersisting = volumeConfirmed && volumeMaintained
        
        state["volumeCarry"] = volumeCarry
        state["volumePersisting"] = volumePersisting
        
        val passed = volumePersisting
        val confidence = if (passed) volumeCarry else volumeConfirmationScore * 0.5
        
        val reason = String.format(
            Locale.US,
            "VolumeContinuation: %s - %s (ratio=%.2f, carry=%.2f, confirmed=%s)",
            if (volumeMaintained) "MAINTAINED" else "DECLINING",
            if (passed) "PASS" else "FAIL",
            volumeRatio,
            volumeCarry,
            volumeConfirmed
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
