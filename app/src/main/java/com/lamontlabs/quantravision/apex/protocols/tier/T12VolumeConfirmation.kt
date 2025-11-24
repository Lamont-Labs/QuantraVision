package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import java.util.Locale
import com.lamontlabs.quantravision.apex.models.*
import kotlin.math.max
/**
 * T12: VolumeConfirmation
 * Purpose: Confirms volume supports pattern validity
 * Category: Momentum & Alignment
 * 
 * BATCH 8 FIX: Uses actual candle volume data, fully deterministic
 */
class T12VolumeConfirmation : ApexProtocol {
    override val protocolId = "T12"
    override val protocolName = "VolumeConfirmation"
    override val weight = 1.5
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val volumes = primitives.candles.map { it.volume }
        
        if (volumes.isEmpty() || volumes.all { it == 0.0 }) {
            state["volumeConfirmed"] = true
            state["volumeConfirmationScore"] = 0.5
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = true,
                confidence = 0.5,
                reason = "Volume confirmation: 0.50 - no volume data",
                weight = weight
            )
        }
        val avgVolume = volumes.average()
        
        // Check if recent candles have higher volume (indicating strength)
        val recentWindow = minOf(5, volumes.size)  // Safe: use available candles
        val recentVolumes = if (volumes.size >= recentWindow) {
            volumes.takeLast(recentWindow)
        } else {
            volumes
        }
        val recentAvg = if (recentVolumes.isNotEmpty()) recentVolumes.average() else avgVolume
        val volumeRatio = if (avgVolume > 0) recentAvg / avgVolume else 1.0
        // Volume confirmation score based on ratio
        val confirmationScore = when {
            volumeRatio >= 1.2 -> 1.0  // 20% higher than average - strong confirmation
            volumeRatio >= 1.0 -> 0.8  // Average or above
            volumeRatio >= 0.8 -> 0.6  // Slightly below average
            else -> 0.4                // Weak volume
        }
        val isConfirmed = confirmationScore >= 0.6
        state["volumeConfirmed"] = isConfirmed
        state["volumeConfirmationScore"] = confirmationScore
        val passed = isConfirmed
        val confidence = confirmationScore
        val strength = when {
            confirmationScore >= 0.8 -> "strong"
            confirmationScore >= 0.6 -> "moderate"
            else -> "weak"
        }
        val reason = "Volume confirmation: ${"%.2f".format(Locale.US, confirmationScore)} - $strength"
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
