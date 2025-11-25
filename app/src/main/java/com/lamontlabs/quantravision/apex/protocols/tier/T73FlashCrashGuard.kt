package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T73FlashCrashGuard : ApexProtocol {
    override val protocolId = "T73"
    override val protocolName = "FlashCrashGuard"
    override val weight = 3.3
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["flashCrashDetected"] = true
            state["flashCrashRisk"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "FlashCrashGuard: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val volumeAnomalyDetected = state["volumeAnomalyDetected"] as? Boolean ?: false
        val abnormalMovementFlag = state["abnormalMovementFlag"] as? Boolean ?: false
        
        val recentCandles = primitives.candles.takeLast(10)
        var flashCrashDetected = volumeAnomalyDetected
        
        if (recentCandles.size >= 10) {
            for (i in 0 until (recentCandles.size - 3)) {
                val candle1 = recentCandles[i]
                val candle2 = recentCandles[i + 1]
                val candle3 = recentCandles[i + 2]
                val candle4 = recentCandles[i + 3]
                
                val avgPrice = (candle1.close + candle2.close + candle3.close) / 3.0
                if (avgPrice == 0.0) continue
                
                val move1 = abs(candle2.close - candle1.close) / avgPrice
                val move2 = abs(candle3.close - candle2.close) / avgPrice
                val reversal = abs(candle4.close - candle3.close) / avgPrice
                
                if (move1 > 0.05 && move2 > 0.05 && reversal > 0.04) {
                    val direction1 = candle2.close - candle1.close
                    val direction2 = candle3.close - candle2.close
                    val directionReversal = candle4.close - candle3.close
                    
                    if ((direction1 * direction2 > 0) && (direction2 * directionReversal < 0)) {
                        flashCrashDetected = true
                        break
                    }
                }
            }
        }
        
        val flashCrashRisk = if (flashCrashDetected) 1.0 else 0.0
        
        state["flashCrashDetected"] = flashCrashDetected
        state["flashCrashRisk"] = flashCrashRisk
        
        val passed = !flashCrashDetected
        val confidence = if (flashCrashDetected) 0.0 else 1.0
        
        val reason = String.format(
            Locale.US,
            "FlashCrashGuard: %s (volAnomaly=%s, abnormal=%s) - %s",
            if (flashCrashDetected) "DETECTED" else "CLEAN",
            volumeAnomalyDetected,
            abnormalMovementFlag,
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
