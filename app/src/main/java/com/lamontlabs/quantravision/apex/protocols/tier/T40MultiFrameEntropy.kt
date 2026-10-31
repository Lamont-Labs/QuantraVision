package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class T40MultiFrameEntropy : ApexProtocol {
    override val protocolId = "T40"
    override val protocolName = "MultiFrameEntropy"
    override val weight = 2.4
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["multiFrameEntropy"] = 1.0
            state["multiFrameEntropyOk"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "MultiFrameEntropy: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val originalEntropy = calculateEntropy(primitives.candles)
        
        val stride2Candles = primitives.candles.filterIndexed { index, _ -> index % 2 == 0 }
        val stride2Entropy = if (stride2Candles.size >= 10) {
            calculateEntropy(stride2Candles)
        } else {
            1.0
        }
        
        val stride3Candles = primitives.candles.filterIndexed { index, _ -> index % 3 == 0 }
        val stride3Entropy = if (stride3Candles.size >= 10) {
            calculateEntropy(stride3Candles)
        } else {
            1.0
        }
        
        val multiFrameEntropy = maxOf(originalEntropy, stride2Entropy, stride3Entropy)
        
        state["multiFrameEntropy"] = multiFrameEntropy
        state["multiFrameEntropyOk"] = multiFrameEntropy < 0.6
        
        val passed = multiFrameEntropy < 0.6
        val confidence = (1.0 - multiFrameEntropy).coerceIn(0.0, 1.0)
        
        val reason = String.format(
            Locale.US,
            "MultiFrameEntropy: %.2f (threshold 0.6) - %s (orig=%.2f, s2=%.2f, s3=%.2f)",
            multiFrameEntropy,
            if (passed) "PASS" else "FAIL",
            originalEntropy,
            stride2Entropy,
            stride3Entropy
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
    
    private fun calculateEntropy(candles: List<Candle>): Double {
        if (candles.size < 2) return 1.0
        val changes = candles.zipWithNext { a, b -> b.close - a.close }.filter { it != 0.0 }
        if (changes.isEmpty()) return 0.0
        val up = changes.count { it > 0 }.toDouble() / changes.size
        if (up == 0.0 || up == 1.0) return 0.0
        return -(up * kotlin.math.ln(up) + (1 - up) * kotlin.math.ln(1 - up)) / kotlin.math.ln(2.0)
    }
}
