package com.lamontlabs.quantravision.apex

import com.lamontlabs.quantravision.apex.models.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ln
import kotlin.math.max

/** Deterministic chart assessment. Image geometry never becomes a confirmed market signal. */
object ApexEngineMobile {
    suspend fun runScan(chartContext: ApexScanContext, primitives: ChartPrimitives, timestamp: Long): ApexResult =
        withContext(Dispatchers.Default) {
            val scanId = ProofHasher.generateScanId(timestamp, "${chartContext.ticker}|${chartContext.timeframe}|${chartContext.chartType}")
            val inputHash = ProofHasher.hashScanInput(chartContext, primitives)
            fun result(status: ApexStatus, trace: List<ProtocolVerdict>, score: Double = 0.0,
                entropy: Double = 1.0, suppression: Boolean = false, regime: Boolean = false,
                confidence: Double = 0.0, reasons: List<String> = emptyList()): ApexResult {
                val value = ApexResult(scanId, status, QuantraScoreMobile.createSnapshot(score), trace,
                    entropy, suppression, status == ApexStatus.OMEGA, regime, reasons,
                    confidence, "", timestamp, inputHash)
                return value.copy(proofHash = ProofHasher.hashApexResult(value))
            }
            try {
                val state = mutableMapOf<String, Any>()
                val guards = ProtocolRegistryMobile.getOmegaProtocols().map { it.evaluate(chartContext, primitives, state) }
                if (guards.size != 4 || guards.any { !it.passed }) {
                    return@withContext result(ApexStatus.OMEGA, guards,
                        reasons = guards.filter { !it.passed }.map { it.reason }.ifEmpty { listOf("Scan guards unavailable") })
                }
                if (primitives.source == PrimitiveSource.SCREEN_GEOMETRY) {
                    val trace = guards + ProtocolVerdict("IMG01", "ObservedScreenGeometry", true, 1.0,
                        "${primitives.candles.size} candle bodies and wicks measured in image pixels", 1.0) +
                        ProtocolVerdict("IMG02", "MarketDataAvailability", false, 0.0,
                            "Market prices, volume and candle timestamps are unavailable from image geometry", 1.0)
                    // WAIT is a deliberate ceiling: image matches are useful but are
                    // never reported as a verified OHLCV/market-regime assessment.
                    return@withContext result(ApexStatus.WAIT, trace, score = 0.0,
                        entropy = 1.0, confidence = 0.0,
                        reasons = listOf("Visual analysis only; price levels are unavailable"))
                }
                val tier = ProtocolRegistryMobile.getTierProtocols().map { it.evaluate(chartContext, primitives, state) }
                val learning = ProtocolRegistryMobile.getLearningProtocols().map { it.evaluate(chartContext, primitives, state) }
                val trace = guards + tier + learning
                val score = weightedScore(tier + learning)
                val confidence = weightedConfidence(tier + learning)
                val entropy = max((state["finalEntropy"] as? Double ?: 0.0), uncertainty(tier + learning))
                    .let { if (it.isFinite()) it.coerceIn(0.0, 1.0) else 1.0 }
                val suppression = state["suppressionActive"] == true ||
                    tier.any { it.protocolId in setOf("T51", "T52", "T53", "T54", "T55") && !it.passed }
                val regime = state["regimeMatch"] == true
                val status = when {
                    suppression -> ApexStatus.SUPPRESSED
                    score < 0.50 -> ApexStatus.FAIL
                    entropy > 0.60 || confidence < 0.70 || !regime || score < 0.70 -> ApexStatus.WAIT
                    else -> ApexStatus.PASS
                }
                result(status, trace, score, entropy, suppression, regime, confidence,
                    trace.filter { !it.passed }.map { "${it.protocolId}: ${it.reason}" }.take(12))
            } catch (cancel: CancellationException) { throw cancel
            } catch (error: Exception) {
                result(ApexStatus.FAIL, emptyList(), reasons = listOf("Analysis failed: ${error.javaClass.simpleName}"))
            }
        }

    internal fun weightedScore(verdicts: List<ProtocolVerdict>): Double {
        val weight = verdicts.sumOf { it.weight }
        if (weight <= 0.0 || !weight.isFinite()) return 0.0
        if (verdicts.any { !it.weight.isFinite() || it.weight < 0 || !it.confidence.isFinite() || it.confidence !in 0.0..1.0 }) return 0.0
        return (verdicts.sumOf { if (it.passed) it.weight * it.confidence else 0.0 } / weight).coerceIn(0.0, 1.0)
    }

    private fun weightedConfidence(verdicts: List<ProtocolVerdict>): Double = weightedScore(verdicts)

    private fun uncertainty(verdicts: List<ProtocolVerdict>): Double {
        val weight = verdicts.sumOf { it.weight }
        if (weight <= 0.0 || !weight.isFinite()) return 1.0
        val p = (verdicts.filter { it.passed }.sumOf { it.weight } / weight).coerceIn(0.0, 1.0)
        val disagreement = if (p <= 0.0 || p >= 1.0) 0.0 else -(p * ln(p) + (1 - p) * ln(1 - p)) / ln(2.0)
        return max(disagreement, 1.0 - weightedConfidence(verdicts))
    }
}
