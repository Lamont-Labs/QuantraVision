package com.lamontlabs.quantravision.apex.protocols.tier

import com.lamontlabs.quantravision.apex.ProtocolRegistryMobile.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class T39TemporalCoherence : ApexProtocol {
    override val protocolId = "T39"
    override val protocolName = "TemporalCoherence"
    override val weight = 2.3
    
    override suspend fun evaluate(
        context: ApexScanContext,
        primitives: ChartPrimitives,
        state: MutableMap<String, Any>
    ): ProtocolVerdict {
        if (primitives.candles.size < 30) {
            state["temporalCoherenceScore"] = 0.0
            state["temporalCoherent"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "TemporalCoherence: Insufficient candles (need >=30, got ${primitives.candles.size})",
                weight = weight
            )
        }
        
        val segmentSize = primitives.candles.size / 3
        if (segmentSize < 10) {
            state["temporalCoherenceScore"] = 0.0
            state["temporalCoherent"] = false
            return ProtocolVerdict(
                protocolId = protocolId,
                protocolName = protocolName,
                passed = false,
                confidence = 0.0,
                reason = "TemporalCoherence: Insufficient segment size (need >=10, got $segmentSize)",
                weight = weight
            )
        }
        
        val segment1 = primitives.candles.take(segmentSize).map { it.close }
        val segment2 = primitives.candles.drop(segmentSize).take(segmentSize).map { it.close }
        val segment3 = primitives.candles.takeLast(segmentSize).map { it.close }
        
        val corr12 = calculateCorrelation(segment1, segment2)
        val corr23 = calculateCorrelation(segment2, segment3)
        val corr13 = calculateCorrelation(segment1, segment3)
        
        val avgCorrelation = (corr12 + corr23 + corr13) / 3.0
        val temporalCoherenceScore = (avgCorrelation + 1.0) / 2.0
        
        state["temporalCoherenceScore"] = temporalCoherenceScore
        state["temporalCoherent"] = temporalCoherenceScore >= 0.6
        
        val passed = temporalCoherenceScore >= 0.6
        val confidence = temporalCoherenceScore
        
        val reason = String.format(
            Locale.US,
            "TemporalCoherence: %.2f (threshold 0.6) - %s (avgCorr=%.2f)",
            temporalCoherenceScore,
            if (passed) "PASS" else "FAIL",
            avgCorrelation
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
    
    private fun calculateCorrelation(x: List<Double>, y: List<Double>): Double {
        if (x.isEmpty() || y.isEmpty() || x.size != y.size) return 0.0
        
        val n = x.size
        val meanX = x.average()
        val meanY = y.average()
        
        var sumXY = 0.0
        var sumX2 = 0.0
        var sumY2 = 0.0
        
        for (i in 0 until n) {
            val dx = x[i] - meanX
            val dy = y[i] - meanY
            sumXY += dx * dy
            sumX2 += dx.pow(2)
            sumY2 += dy.pow(2)
        }
        
        val denominator = sqrt(sumX2 * sumY2)
        return if (denominator > 0.0) sumXY / denominator else 0.0
    }
}
