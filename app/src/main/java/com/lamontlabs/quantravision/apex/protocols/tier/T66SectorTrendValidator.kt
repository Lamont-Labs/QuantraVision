package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T66SectorTrendValidator : ApexProtocol {
    override val protocolId = "T66"
    override val protocolName = "SectorTrendValidator"
    override val weight = 3.1
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["sectorTrendValid"] = false
            state["sectorTrendScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "SectorTrendValidator: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val sectorCompatible = state["sectorCompatible"] as? Boolean
        val trendDirection = state["trendDirection"] as? Double
        
        // DETECT missing upstream state - FAIL CLOSED
        if (sectorCompatible == null || trendDirection == null) {
            state["sectorTrendValid"] = false
            state["sectorTrendScore"] = 0.0
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "SectorTrendValidator: Missing upstream state (sectorCompatible or trendDirection) - FAIL (fail-closed)",
                weight = weight
            )
        }
        
        // NOW can use non-null values
        val sectorTrendValid: Boolean
        val sectorTrendScore: Double
        
        if (primitives.detectedLines.isEmpty()) {
            // No lines available - use neutral but allow analysis
            // This is OK because we have sector compatibility data
            sectorTrendScore = if (sectorCompatible) 0.6 else 0.2
            sectorTrendValid = sectorCompatible  // Respect sector compatibility
        } else {
            val avgSlope = primitives.detectedLines.map { line ->
                if (abs(line.x2 - line.x1) < 0.001) {
                    0.0
                } else {
                    (line.y2 - line.y1) / (line.x2 - line.x1)
                }
            }.average()
            
            val slopesMatchTrend = (avgSlope > 0 && trendDirection > 0) ||
                                  (avgSlope < 0 && trendDirection < 0) ||
                                  (abs(avgSlope) < 0.01 && abs(trendDirection) < 0.01)
            
            sectorTrendScore = if (slopesMatchTrend && sectorCompatible) 0.85 else 0.3
            sectorTrendValid = sectorTrendScore >= 0.6
        }
        
        state["sectorTrendValid"] = sectorTrendValid
        state["sectorTrendScore"] = sectorTrendScore
        
        val passed = sectorTrendValid
        val confidence = sectorTrendScore
        
        val reason = String.format(
            Locale.US,
            "SectorTrendValidator: %.2f (lines=%d, compatible=%s) - %s",
            sectorTrendScore,
            primitives.detectedLines.size,
            sectorCompatible,
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
