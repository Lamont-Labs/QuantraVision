package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T60MarketStressIndicator : ApexProtocol {
    override val protocolId = "T60"
    override val protocolName = "MarketStressIndicator"
    override val weight = 3.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["marketStressLevel"] = 1.0
            state["volatilityGuardOk"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "MarketStressIndicator: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val volatilityException = state["volatilityException"] as? Boolean ?: false
        val volatilitySpikeDetected = state["volatilitySpikeDetected"] as? Boolean ?: false
        val abnormalMovementFlag = state["abnormalMovementFlag"] as? Boolean ?: false
        val volumeAnomalyDetected = state["volumeAnomalyDetected"] as? Boolean ?: false
        val regimeShiftDetected = state["regimeShiftDetected"] as? Boolean ?: false
        
        val volatilityExceptionScore = state["volatilityExceptionScore"] as? Double ?: 0.5
        val spikeIntensity = state["spikeIntensity"] as? Double ?: 0.5
        val movementAbnormalityScore = state["movementAbnormalityScore"] as? Double ?: 0.5
        val volumeAnomalyScore = state["volumeAnomalyScore"] as? Double ?: 0.5
        
        val exceptionCount = listOf(
            volatilityException,
            volatilitySpikeDetected,
            abnormalMovementFlag,
            volumeAnomalyDetected,
            regimeShiftDetected
        ).count { it }
        
        val avgExceptionScore = (volatilityExceptionScore + spikeIntensity + 
                                 movementAbnormalityScore + volumeAnomalyScore) / 4.0
        
        val marketStressLevel = when {
            exceptionCount >= 4 -> 0.95
            exceptionCount >= 3 -> 0.85
            exceptionCount >= 2 -> 0.70
            exceptionCount >= 1 -> avgExceptionScore.coerceIn(0.4, 0.7)
            else -> avgExceptionScore.coerceIn(0.0, 0.5)
        }
        
        val volatilityGuardOk = marketStressLevel < 0.7
        
        state["marketStressLevel"] = marketStressLevel
        state["volatilityGuardOk"] = volatilityGuardOk
        
        val passed = volatilityGuardOk
        val confidence = 1.0 - marketStressLevel
        
        val reason = String.format(
            Locale.US,
            "MarketStressIndicator: %.2f (exceptions=%d/5, threshold=0.7) - %s",
            marketStressLevel,
            exceptionCount,
            if (passed) "PASS" else "STRESS_HIGH"
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
