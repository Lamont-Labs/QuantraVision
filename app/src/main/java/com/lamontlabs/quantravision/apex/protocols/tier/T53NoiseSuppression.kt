package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T53NoiseSuppression : ApexProtocol {
    override val protocolId = "T53"
    override val protocolName = "NoiseSuppression"
    override val weight = 2.8
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 20) {
            state["noiseSuppressed"] = true
            state["noiseSuppressionLevel"] = 1.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "NoiseSuppression: Insufficient candles (need >=20, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val signalClarity = state["signalClarity"] as? Boolean ?: false
        val signalClarityScore = state["signalClarityScore"] as? Double ?: 0.0
        
        val shouldSuppress = !signalClarity || signalClarityScore < 0.6
        
        val noiseSuppressed = shouldSuppress
        
        val noiseSuppressionLevel = when {
            !signalClarity && signalClarityScore < 0.4 -> 0.9
            !signalClarity -> 0.8
            signalClarityScore < 0.4 -> 0.75
            signalClarityScore < 0.6 -> 0.6
            else -> 0.2
        }
        
        state["noiseSuppressed"] = noiseSuppressed
        state["noiseSuppressionLevel"] = noiseSuppressionLevel
        
        val passed = !noiseSuppressed
        val confidence = if (signalClarity) signalClarityScore else 0.0
        
        val reason = String.format(
            Locale.US,
            "NoiseSuppression: %.2f (clarity=%s, clarityScore=%.2f) - %s",
            noiseSuppressionLevel,
            signalClarity,
            signalClarityScore,
            if (passed) "PASS" else "SUPPRESSED"
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
