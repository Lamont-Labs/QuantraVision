package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
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
        
        val multiFrameEntropy = (originalEntropy + stride2Entropy + stride3Entropy) / 3.0
        
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
        if (candles.isEmpty()) return 1.0
        
        val prices = candles.map { it.close }
        val mean = prices.average()
        val variance = prices.map { (it - mean).pow(2) }.average()
        val stdDev = sqrt(variance)
        
        return if (mean > 0.0) {
            (stdDev / mean).coerceIn(0.0, 1.0)
        } else 0.5
    }
}
