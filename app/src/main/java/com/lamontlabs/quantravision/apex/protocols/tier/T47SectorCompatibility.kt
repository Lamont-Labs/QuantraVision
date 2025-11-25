package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.abs

class T47SectorCompatibility : ApexProtocol {
    override val protocolId = "T47"
    override val protocolName = "SectorCompatibility"
    override val weight = 2.7
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 25) {
            state["sectorCompatibilityScore"] = 0.0
            state["sectorCompatible"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "SectorCompatibility: Insufficient candles (need >=25, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val trendDirection = state["trendDirection"] as? String ?: "UNKNOWN"
        val volatility = state["volatility"] as? Double ?: 0.05
        
        val detectedLines = primitives.detectedLines
        
        val sectorCompatibilityScore = if (detectedLines.isNotEmpty()) {
            val trendlineSlopes = detectedLines.map { line ->
                if (line.x2 > line.x1 && line.x2 - line.x1 > 0) {
                    (line.y2 - line.y1) / (line.x2 - line.x1)
                } else {
                    0.0
                }
            }
            
            val avgSlope = trendlineSlopes.average()
            
            val variance = trendlineSlopes.map { (it - avgSlope) * (it - avgSlope) }.average()
            val slopeConsistency = 1.0 / (1.0 + variance)
            
            when {
                trendDirection == "UP" && avgSlope > 0 -> (0.7 + slopeConsistency * 0.3).coerceIn(0.0, 1.0)
                trendDirection == "DOWN" && avgSlope < 0 -> (0.7 + slopeConsistency * 0.3).coerceIn(0.0, 1.0)
                trendDirection == "SIDEWAYS" && abs(avgSlope) < 0.1 -> 0.75
                else -> 0.45
            }
        } else {
            0.6
        }
        
        state["sectorCompatibilityScore"] = sectorCompatibilityScore
        state["sectorCompatible"] = sectorCompatibilityScore >= 0.5
        
        val passed = sectorCompatibilityScore >= 0.5
        val confidence = sectorCompatibilityScore
        
        val reason = if (detectedLines.isNotEmpty()) {
            val avgSlope = detectedLines.map { line ->
                if (line.x2 > line.x1 && line.x2 - line.x1 > 0) {
                    (line.y2 - line.y1) / (line.x2 - line.x1)
                } else {
                    0.0
                }
            }.average()
            val variance = detectedLines.map { line ->
                val slope = if (line.x2 > line.x1 && line.x2 - line.x1 > 0) {
                    (line.y2 - line.y1) / (line.x2 - line.x1)
                } else {
                    0.0
                }
                (slope - avgSlope) * (slope - avgSlope)
            }.average()
            val slopeConsistency = 1.0 / (1.0 + variance)
            String.format(
                Locale.US,
                "SectorCompatibility: %.2f (trend=%s, slope=%.3f, consistency=%.2f) - %s",
                sectorCompatibilityScore,
                trendDirection,
                avgSlope,
                slopeConsistency,
                if (passed) "PASS" else "FAIL"
            )
        } else {
            String.format(
                Locale.US,
                "SectorCompatibility: %.2f (trend=%s, noData=true, defaultPermissive) - %s",
                sectorCompatibilityScore,
                trendDirection,
                if (passed) "PASS" else "FAIL"
            )
        }
        
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
