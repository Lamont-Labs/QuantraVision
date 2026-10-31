package com.lamontlabs.quantravision.apex.protocols.omega

import com.lamontlabs.quantravision.apex.ApexProtocol
import com.lamontlabs.quantravision.apex.models.*

/** Guards for chart analysis. They do not claim to validate account/position risk. */
object ChartScanGuards {
    fun all(): List<ApexProtocol> = listOf(
        Guard("Omega01", "CandleCompleteness") { _, p ->
            if (p.candles.size >= 10) null else "At least 10 measured candles are required"
        },
        Guard("Omega02", "MeasurementIntegrity") { _, p ->
            if (p.candles.all { c ->
                listOf(c.open, c.high, c.low, c.close, c.volume).all { it.isFinite() } &&
                    c.low > 0 && c.high >= c.low && c.open in c.low..c.high &&
                    c.close in c.low..c.high && c.volume >= 0
            }) null else "Non-finite or inconsistent candle measurements"
        },
        Guard("Omega03", "ObservationProvenance") { c, p ->
            when {
                !Regex("[0-9a-fA-F]{16}|[0-9a-fA-F]{64}").matches(p.rawImageHash) -> "Missing or malformed image provenance"
                c.userId.isBlank() -> "Missing scan identity"
                !p.candles.zipWithNext().all { (a, b) -> b.timestamp > a.timestamp } -> "Candle sequence is not strictly increasing"
                else -> null
            }
        },
        Guard("Omega04", "SourceBoundary") { c, p ->
            when {
                c.chartType != p.chartType -> "Chart context does not match observations"
                p.source == PrimitiveSource.SCREEN_GEOMETRY && (p.imageWidth == null || p.imageWidth < 160 ||
                    p.imageHeight == null || p.imageHeight < 120) -> "Missing image dimensions"
                p.source == PrimitiveSource.SCREEN_GEOMETRY && p.candles.any {
                    it.high > (p.imageHeight ?: 0) + 1.0 || it.volume != 0.0
                } -> "Image geometry contains out-of-bounds coordinates or unobserved volume"
                p.detectedLines.any { line ->
                    !listOf(line.x1, line.x2, line.y1, line.y2, line.confidence).all { it.isFinite() } ||
                        line.confidence !in 0.0..1.0
                } -> "Invalid boundary measurements"
                else -> null
            }
        }
    )

    private class Guard(
        override val protocolId: String,
        override val protocolName: String,
        private val failure: (ApexScanContext, ChartPrimitives) -> String?
    ) : ApexProtocol {
        override val weight = 1.0
        override suspend fun evaluate(context: ApexScanContext, primitives: ChartPrimitives,
            state: MutableMap<String, Any>): ProtocolVerdict {
            val reason = failure(context, primitives)
            state[protocolId + "_passed"] = reason == null
            return ProtocolVerdict(protocolId, protocolName, reason == null,
                if (reason == null) 1.0 else 0.0, reason ?: "$protocolName passed", weight)
        }
    }
}
