package com.lamontlabs.quantravision.analysis

import com.lamontlabs.quantravision.detection.Detection
import kotlin.math.max
import kotlin.math.min

enum class TradeabilityLabel { NOT_VIABLE, CAUTION, VIABLE }

data class TradeabilityInput(
  val detection: Detection,
  val mtfConfluence: Float?,     // null if not available
  val volRegime: Float,          // 0..1 (normalized ATR % of price or bar height dispersion)
  val liquidityProxy: Float      // 0..1 (scaled from OCR volume or bar density)
)

data class TradeabilityResult(
  val score: Float,              // 0..1
  val label: TradeabilityLabel
)

class TradeabilityEngine {
  fun evaluate(input: TradeabilityInput): TradeabilityResult {
    val conf = input.detection.confidence.coerceIn(0f, 1f)
    val mtf  = (input.mtfConfluence ?: 0.5f).coerceIn(0f, 1f)  // neutral if absent
    val volA = adjustVol(input.volRegime)                      // prefer mid volatility
    val liqA = input.liquidityProxy.coerceIn(0f, 1f)

    val score = 0.45f*conf + 0.25f*mtf + 0.15f*volA + 0.15f*liqA
    val label = when {
      score < 0.55f -> TradeabilityLabel.NOT_VIABLE
      score < 0.70f -> TradeabilityLabel.CAUTION
      else -> TradeabilityLabel.VIABLE
    }
    return TradeabilityResult(score = score, label = label)
  }

  private fun adjustVol(v: Float): Float {
    // bell-shaped preference: too low or too high vol is penalized
    val x = v.coerceIn(0f, 1f)
    val center = 0.5f
    val dist = kotlin.math.abs(x - center)
    return 1f - min(1f, dist * 2f) * 0.6f // 1 at center, down to 0.4 at extremes
  }
}
