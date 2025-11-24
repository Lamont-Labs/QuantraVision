package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T74LiquidityStressDetector : ApexProtocol {
    override val protocolId = "T74"
    override val protocolName = "LiquidityStressDetector"
    override val weight = 3.35
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["liquidityStress"] = true
            state["stressLevel"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "LiquidityStressDetector: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val volumeAnomalyDetected = state["volumeAnomalyDetected"] as? Boolean ?: false
        
        val recentCandles = primitives.candles.takeLast(10)
        var liquidityStress = volumeAnomalyDetected
        var stressLevel = 0.0
        
        if (recentCandles.size >= 10) {
            val volumes = recentCandles.map { it.volume }
            val volatilities = recentCandles.map { abs(it.high - it.low) }
            
            if (volumes.all { it > 0 } && volatilities.isNotEmpty()) {
                val volumeTrend = if (volumes.size >= 5) {
                    val firstHalf = volumes.take(5).average()
                    val secondHalf = volumes.takeLast(5).average()
                    if (firstHalf > 0) (secondHalf - firstHalf) / firstHalf else 0.0
                } else {
                    0.0
                }
                
                val volatilityTrend = if (volatilities.size >= 5) {
                    val firstHalf = volatilities.take(5).average()
                    val secondHalf = volatilities.takeLast(5).average()
                    if (firstHalf > 0) (secondHalf - firstHalf) / firstHalf else 0.0
                } else {
                    0.0
                }
                
                liquidityStress = volumeTrend < -0.2 && volatilityTrend > 0.2
                stressLevel = if (liquidityStress) 1.0 else 0.0
            } else {
                liquidityStress = true
                stressLevel = 1.0
            }
        }
        
        state["liquidityStress"] = liquidityStress
        state["stressLevel"] = stressLevel
        
        val passed = !liquidityStress
        val confidence = 1.0 - stressLevel
        
        val reason = String.format(
            Locale.US,
            "LiquidityStressDetector: %s (level=%.2f) - %s",
            if (liquidityStress) "STRESSED" else "HEALTHY",
            stressLevel,
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
