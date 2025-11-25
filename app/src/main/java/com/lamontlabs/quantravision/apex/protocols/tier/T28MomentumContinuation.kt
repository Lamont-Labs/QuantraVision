package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T28MomentumContinuation : ApexProtocol {
    override val protocolId = "T28"
    override val protocolName = "MomentumContinuation"
    override val weight = 1.9
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 15) {
            state["momentumCarry"] = 0.0
            state["momentumPersisting"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "MomentumContinuation: Insufficient candles (need >=15, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val momentumAligned = state["momentumAligned"] as? Boolean ?: false
        val momentumScore = state["momentumScore"] as? Double ?: 0.5
        
        val last5 = primitives.candles.takeLast(5)
        var positiveMoves = 0
        var negativeMoves = 0
        
        for (i in 1 until last5.size) {
            val change = last5[i].close - last5[i - 1].close
            if (change > 0) positiveMoves++ else if (change < 0) negativeMoves++
        }
        
        val recentMomentumDirection = when {
            positiveMoves > negativeMoves -> "UP"
            negativeMoves > positiveMoves -> "DOWN"
            else -> "FLAT"
        }
        
        val consistencyScore = if (last5.size > 1) {
            maxOf(positiveMoves, negativeMoves).toDouble() / (last5.size - 1)
        } else 0.5
        
        val momentumCarry = momentumScore * consistencyScore
        val momentumPersisting = momentumAligned && consistencyScore >= 0.6
        
        state["momentumCarry"] = momentumCarry
        state["momentumPersisting"] = momentumPersisting
        
        val passed = momentumPersisting
        val confidence = if (passed) momentumCarry else momentumScore * 0.4
        
        val reason = String.format(
            Locale.US,
            "MomentumContinuation: %s - %s (score=%.2f, carry=%.2f, consistency=%.2f)",
            recentMomentumDirection,
            if (passed) "PASS" else "FAIL",
            momentumScore,
            momentumCarry,
            consistencyScore
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
