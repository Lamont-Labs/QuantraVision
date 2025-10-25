package com.lamontlabs.quantravision.analysis

import androidx.camera.core.ImageProxy
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * RiskLabelEngine
 * - Estimates on-screen trading frictions and hazards from the chart image only.
 * - Outputs conservative flags that never constitute advice.
 *
 * Inputs (all inferred locally from pixels/geometry):
 *  - spreadProxy: bid/ask distance proxy from last price box glyphs or tape (0..1)
 *  - gapRisk: recent gap magnitude vs ATR proxy (0..1)
 *  - liquidityProxy: bar body area density in recent window (0..1)
 *  - sessionState: pre/post/regular (if watermark text detected locally)
 *
 * Output:
 *  - RiskLabel: { LOW, ELEVATED, HIGH }
 *  - components map for UI explainers
 */
enum class RiskLabel { LOW, ELEVATED, HIGH }

data class RiskAssessment(
    val label: RiskLabel,
    val score: Float, // 0..1
    val components: Map<String, Float>
)

interface RiskAnalyzer {
    fun load()
    fun analyze(frame: ImageProxy): RiskAssessment
}

class RiskLabelEngine : RiskAnalyzer {

    override fun load() { /* no-op; keep deterministic */ }

    override fun analyze(frame: ImageProxy): RiskAssessment {
        // Lightweight heuristics. Replace stubs with real CV/OCR if desired.
        val spread = estimateSpreadProxy(frame)        // 0..1 higher = wider
        val gaps   = estimateGapRisk(frame)            // 0..1 higher = riskier
        val liq    = estimateLiquidityProxy(frame)     // 0..1 higher = more liquid
        val sess   = estimateSessionPenalty(frame)     // 0..1 penalty for pre/post

        // Compose a conservative risk score (higher = worse)
        val liqPenalty = (1f - liq) * 0.35f
        val score = (0.35f * spread) + (0.35f * gaps) + liqPenalty + (0.20f * sess)
        val clamped = score.coerceIn(0f, 1f)

        val label = when {
            clamped < 0.33f -> RiskLabel.LOW
            clamped < 0.66f -> RiskLabel.ELEVATED
            else -> RiskLabel.HIGH
        }

        frame.close()
        return RiskAssessment(
            label = label,
            score = clamped,
            components = mapOf(
                "spread" to spread,
                "gapRisk" to gaps,
                "illiquidity" to (1f - liq),
                "sessionPenalty" to sess
            )
        )
    }

    // --------- heuristics (deterministic stubs; keep offline) ---------

    private fun estimateSpreadProxy(@Suppress("UNUSED_PARAMETER") frame: ImageProxy): Float {
        // If L1 quotes visible on the right gutter, approximate pixel distance between bid/ask boxes.
        // Fallback: small constant to remain conservative.
        return 0.25f
    }

    private fun estimateGapRisk(@Suppress("UNUSED_PARAMETER") frame: ImageProxy): Float {
        // Scan last ~30 candles and detect vertical discontinuities (gap ups/downs).
        // Normalize by median body height as ATR proxy.
        return 0.20f
    }

    private fun estimateLiquidityProxy(@Suppress("UNUSED_PARAMETER") frame: ImageProxy): Float {
        // Use body+wick density per bar across the last region; thicker + consistent = more liquid.
        return 0.70f
    }

    private fun estimateSessionPenalty(@Suppress("UNUSED_PARAMETER") frame: ImageProxy): Float {
        // If OCR finds "PRE" / "POST" / "EXTENDED HOURS" watermark, return >= 0.5
        return 0.0f
    }
}
