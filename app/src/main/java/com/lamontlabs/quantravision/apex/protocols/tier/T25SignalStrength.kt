package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T25SignalStrength : ApexProtocol {
    override val protocolId = "T25"
    override val protocolName = "SignalStrength"
    override val weight = 2.0
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 15) {
            state["netSignalStrength"] = 0.0
            state["signalStrengthOk"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "SignalStrength: Insufficient candles (need >=15, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val trendStrength = state["trendStrength"] as? Double ?: 0.5
        val momentumScore = state["momentumScore"] as? Double ?: 0.5
        val volumeConfirmationScore = state["volumeConfirmationScore"] as? Double ?: 0.5
        
        val netSignalStrength = (trendStrength * 0.4 + momentumScore * 0.35 + volumeConfirmationScore * 0.25)
            .coerceIn(0.0, 1.0)
        
        state["netSignalStrength"] = netSignalStrength
        state["signalStrengthOk"] = netSignalStrength >= 0.65
        
        val passed = netSignalStrength >= 0.65
        val confidence = netSignalStrength
        
        val reason = String.format(
            Locale.US,
            "SignalStrength: %.2f (threshold 0.65) - %s (trend=%.2f, momentum=%.2f, volume=%.2f)",
            netSignalStrength,
            if (passed) "PASS" else "FAIL",
            trendStrength,
            momentumScore,
            volumeConfirmationScore
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
