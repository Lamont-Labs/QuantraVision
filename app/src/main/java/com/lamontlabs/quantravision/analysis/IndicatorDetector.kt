package com.lamontlabs.quantravision.analysis

import android.graphics.Color
import androidx.camera.core.ImageProxy
import com.lamontlabs.quantravision.detection.Detection
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * IndicatorDetector
 * - Reads common indicators rendered by charting apps without APIs.
 * - Sources: legend OCR hits, color/style cues, subpanel geometry.
 * - Output integrates with overlay labels and tradeability logic.
 *
 * Detected types:
 *  - MA_SMA, MA_EMA, MA_WMA
 *  - BOLLINGER
 *  - VWAP
 *  - RSI, MACD
 *  - VOLUME
 *  - ICHIMOKU
 */
interface IndicatorDetector {
    fun load()
    fun analyze(frame: ImageProxy): List<IndicatorHit>
}

enum class IndicatorType {
    MA_SMA, MA_EMA, MA_WMA,
    BOLLINGER, VWAP,
    RSI, MACD, VOLUME, ICHIMOKU, UNKNOWN
}

data class IndicatorHit(
    val type: IndicatorType,
    val label: String,          // e.g., "EMA(50)", "BB(20,2)"
    val confidence: Float,      // 0..1
    val panel: Panel = Panel.MAIN
)

enum class Panel { MAIN, LOWER_1, LOWER_2 }

/**
 * SimpleIndicatorDetector
 * - Heuristic fusion: legend tokens + style cues + panel inference
 * - Pure on-device; wire your local OCR in recognizeLegend()
 */
class SimpleIndicatorDetector : IndicatorDetector {

    private val legendTokens = listOf(
        "SMA","EMA","WMA","MA","Moving Average","Bollinger","BB",
        "VWAP","RSI","MACD","Ichimoku","Volume","VOL"
    )

    override fun load() { /* no-op; plug OCR model load here if needed */ }

    override fun analyze(frame: ImageProxy): List<IndicatorHit> {
        // 1) Legend OCR (stub). Replace with ML Kit local text-recognizer call.
        val legend = recognizeLegend(frame) // e.g., ["EMA(50)","RSI(14)","VWAP"]
        val hitsFromLegend = legend.flatMap { token -> mapLegendToken(token) }

        // 2) Visual cues fallback when legend hidden
        val cues = detectVisualCues(frame) // coarse hints by color/style/panels

        // 3) Fuse with simple de-duplication and confidence max
        val fused = mutableMapOf<String, IndicatorHit>()
        (hitsFromLegend + cues).forEach { hit ->
            val key = hit.type.name + ":" + hit.label + ":" + hit.panel.name
            val prev = fused[key]
            fused[key] = if (prev == null) hit else {
                if (hit.confidence >= prev.confidence) hit else prev
            }
        }

        frame.close() // we don’t keep frames
        return fused.values.map { it.copy(confidence = it.confidence.coerceIn(0f,1f)) }
    }

    // -------- internals --------

    private fun recognizeLegend(@Suppress("UNUSED_PARAMETER") frame: ImageProxy): List<String> {
        // Stub for local OCR. Keep deterministic.
        // Return empty to rely on visual cues when no legend present.
        return emptyList()
    }

    private fun mapLegendToken(tok: String): List<IndicatorHit> {
        val t = tok.lowercase()
        return when {
            "ema" in t -> listOf(IndicatorHit(IndicatorType.MA_EMA, tok, 0.95f, Panel.MAIN))
            "sma" in t || "moving average" in t || ("ma(" in t && "ema" !in t && "wma" !in t) ->
                listOf(IndicatorHit(IndicatorType.MA_SMA, tok, 0.92f, Panel.MAIN))
            "wma" in t -> listOf(IndicatorHit(IndicatorType.MA_WMA, tok, 0.90f, Panel.MAIN))
            "bollinger" in t || "bb(" in t -> listOf(IndicatorHit(IndicatorType.BOLLINGER, tok, 0.92f, Panel.MAIN))
            "vwap" in t -> listOf(IndicatorHit(IndicatorType.VWAP, tok, 0.93f, Panel.MAIN))
            "rsi" in t -> listOf(IndicatorHit(IndicatorType.RSI, tok, 0.94f, Panel.LOWER_1))
            "macd" in t -> listOf(IndicatorHit(IndicatorType.MACD, tok, 0.94f, Panel.LOWER_1))
            "vol" in t || "volume" in t -> listOf(IndicatorHit(IndicatorType.VOLUME, tok, 0.90f, Panel.LOWER_2))
            "ichimoku" in t -> listOf(IndicatorHit(IndicatorType.ICHIMOKU, tok, 0.91f, Panel.MAIN))
            else -> emptyList()
        }
    }

    private fun detectVisualCues(@Suppress("UNUSED_PARAMETER") frame: ImageProxy): List<IndicatorHit> {
        // Minimal deterministic placeholders:
        // In production, compute per-pixel line counts, band pairs, cloud fills, and subpanel separators.
        val results = mutableListOf<IndicatorHit>()

        // Example heuristics sketch (replace with real CV):
        // - Parallel band pair with semi-transparent fill => Bollinger
        // - Single thick mean-reverting line hugging price => VWAP
        // - Cloud-style filled region spanning ahead => Ichimoku
        // - Lower panes with oscillator wave + midline => RSI/MACD
        // - Dense vertical rectangles at bottom => Volume

        // Conservatively emit low-confidence hints. Router can upweight via legend confirmation.
        // Keep offline and deterministic.
        return results
    }
}
```0
