package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class T58AbnormalMovementDetection : ApexProtocol {
    override val protocolId = "T58"
    override val protocolName = "AbnormalMovementDetection"
    override val weight = 2.9
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        val validPrices = primitives.candles.map { it.close }.filter { it > 0.0 }
        
        if (validPrices.size < 30) {
            state["abnormalMovementFlag"] = true
            state["movementAbnormalityScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "AbnormalMovementDetection: Insufficient valid prices (need >=30, got ${validPrices.size})",
                weight = weight
            )
        }
        
        val priceChanges = mutableListOf<Double>()
        for (i in 1 until validPrices.size) {
            val change = (validPrices[i] - validPrices[i - 1]) / validPrices[i - 1]
            if (!change.isNaN() && !change.isInfinite()) {
                priceChanges.add(change)
            }
        }
        
        if (priceChanges.isEmpty()) {
            state["abnormalMovementFlag"] = true
            state["movementAbnormalityScore"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "AbnormalMovementDetection: No valid price changes - FAIL",
                weight = weight
            )
        }
        
        val mean = priceChanges.average()
        val variance = priceChanges.map { (it - mean).pow(2) }.average()
        val stdDev = sqrt(variance)
        
        val recentChanges = priceChanges.takeLast(minOf(10, priceChanges.size))
        val maxRecentDeviation = if (recentChanges.isNotEmpty()) {
            recentChanges.maxOfOrNull { abs(it - mean) } ?: 0.0
        } else {
            0.0
        }
        
        val deviationInStdDevs = if (stdDev > 0.0) {
            maxRecentDeviation / stdDev
        } else {
            0.0
        }
        
        val abnormalMovementFlag = deviationInStdDevs > 3.0
        
        val movementAbnormalityScore = when {
            deviationInStdDevs > 5.0 -> 0.95
            deviationInStdDevs > 4.0 -> 0.85
            deviationInStdDevs > 3.0 -> 0.70
            deviationInStdDevs > 2.0 -> 0.45
            else -> 0.20
        }
        
        state["abnormalMovementFlag"] = abnormalMovementFlag
        state["movementAbnormalityScore"] = movementAbnormalityScore
        
        val passed = !abnormalMovementFlag
        val confidence = 1.0 - movementAbnormalityScore
        
        val reason = String.format(
            Locale.US,
            "AbnormalMovementDetection: %.2f (deviation=%.1f sigma, stdDev=%.4f) - %s",
            movementAbnormalityScore,
            deviationInStdDevs,
            stdDev,
            if (passed) "PASS" else "ABNORMAL"
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
