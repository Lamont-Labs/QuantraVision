package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale

class T69CrossSectorValidation : ApexProtocol {
    override val protocolId = "T69"
    override val protocolName = "CrossSectorValidation"
    override val weight = 3.25
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["crossSectorValid"] = false
            state["crossSectorScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "CrossSectorValidation: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val sectorCompatible = state["sectorCompatible"] as? Boolean ?: false
        val sectorTrendValid = state["sectorTrendValid"] as? Boolean ?: false
        val regimeAlignmentOk = state["regimeAlignmentOk"] as? Boolean ?: false
        
        val crossSectorScore = ((if (sectorCompatible) 1.0 else 0.0) +
                               (if (sectorTrendValid) 1.0 else 0.0) +
                               (if (regimeAlignmentOk) 1.0 else 0.0)) / 3.0
        
        val crossSectorValid = crossSectorScore >= 0.65
        
        state["crossSectorValid"] = crossSectorValid
        state["crossSectorScore"] = crossSectorScore
        
        val passed = crossSectorScore >= 0.65
        val confidence = crossSectorScore
        
        val reason = String.format(
            Locale.US,
            "CrossSectorValidation: %.2f (compat=%s, trend=%s, regime=%s) - %s",
            crossSectorScore,
            sectorCompatible,
            sectorTrendValid,
            regimeAlignmentOk,
            if (passed) "PASS" else "FAIL"
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
