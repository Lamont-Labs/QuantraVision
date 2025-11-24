package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T37CrossTimeframeValidation : ApexProtocol {
    override val protocolId = "T37"
    override val protocolName = "CrossTimeframeValidation"
    override val weight = 2.1
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["crossFrameConsistency"] = 0.0
            state["crossFrameOk"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "CrossTimeframeValidation: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val originalVolatility = calculateVolatility(primitives.candles)
        
        val stride3Candles = primitives.candles.filterIndexed { index, _ -> index % 3 == 0 }
        if (stride3Candles.size < 10) {
            state["crossFrameConsistency"] = 0.0
            state["crossFrameOk"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "CrossTimeframeValidation: Insufficient stride data (need >=10, got ${stride3Candles.size})",
                weight = weight
            )
        }
        
        val stride3Volatility = calculateVolatility(stride3Candles)
        
        val volatilityDiff = abs(originalVolatility - stride3Volatility)
        val crossFrameConsistency = (1.0 - (volatilityDiff / (originalVolatility + 0.01))).coerceIn(0.0, 1.0)
        
        state["crossFrameConsistency"] = crossFrameConsistency
        state["crossFrameOk"] = crossFrameConsistency >= 0.6
        
        val passed = crossFrameConsistency >= 0.6
        val confidence = crossFrameConsistency
        
        val reason = String.format(
            Locale.US,
            "CrossTimeframeValidation: %.2f (threshold 0.6) - %s (orig=%.4f, stride3=%.4f)",
            crossFrameConsistency,
            if (passed) "PASS" else "FAIL",
            originalVolatility,
            stride3Volatility
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
    
    private fun calculateVolatility(candles: List<Candle>): Double {
        if (candles.isEmpty()) return 0.0
        val ranges = candles.map { it.high - it.low }
        return ranges.average()
    }
}
