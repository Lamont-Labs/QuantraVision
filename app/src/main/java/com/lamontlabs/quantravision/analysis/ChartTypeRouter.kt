package com.lamontlabs.quantravision.analysis

import androidx.camera.core.ImageProxy
import com.lamontlabs.quantravision.detection.Detector
import com.lamontlabs.quantravision.detection.Detection
import kotlin.math.max
import kotlin.math.min

/**
 * Chart-Type Auto-Router v2
 * - Classifies chart style every frame (EMA-smoothed + hysteresis)
 * - Applies per-type detection thresholds and wick/body heuristics
 * - Suppresses jitter when confidence bounces near boundaries
 *
 * Usage:
 *   val router = ChartTypeRouter(baseClassifier, baseDetector)
 *   router.load(context)
 *   val dets = router.analyze(frame)
 */
class ChartTypeRouter(
  private val classifier: ChartTypeClassifier,
  private val backend: Detector
) : Detector {

  // Per-type tuning. Values are conservative; adjust as needed.
  private val cfg = mapOf(
    ChartType.CANDLE to Params(minConf = 0.58f, wickBias = 0.12f, bodyBias = 0.10f),
    ChartType.BAR to Params(minConf = 0.60f, wickBias = 0.08f, bodyBias = 0.06f),
    ChartType.LINE to Params(minConf = 0.65f, wickBias = 0.00f, bodyBias = 0.00f),
    ChartType.HEIKIN_ASHI to Params(minConf = 0.62f, wickBias = 0.06f, bodyBias = 0.14f),
    ChartType.RENKO to Params(minConf = 0.68f, wickBias = 0.00f, bodyBias = 0.18f),
    ChartType.UNKNOWN to Params(minConf = 0.70f, wickBias = 0.00f, bodyBias = 0.00f)
  )

  // EMA + hysteresis for stable type selection
  private var emaCandle = 0f
  private var emaBar = 0f
  private var emaLine = 0f
  private var emaHeikin = 0f
  private var emaRenko = 0f
  private var current: ChartType = ChartType.UNKNOWN

  // Jitter control
  private val alpha = 0.25f          // EMA smoothing factor
  private val hysteresis = 0.06f     // winner margin to switch type
  private var lastTypeSwitchNs = 0L
  private val minHoldNs = 250_000_000L // 250 ms hold before switching

  override fun load(context: android.content.Context) {
    backend.load(context)
  }

  override fun analyze(frame: ImageProxy): List<Detection> {
    // 1) Classify chart type (cheap features inside classifier)
    val t = classifier.classify(frame)
    updateEma(t)
    val routed = decideType(System.nanoTime())

    // 2) Run backend detector (backend is responsible for closing frame)
    val dets = backend.analyze(frame)

    // 3) Apply per-type routing params and jitter suppression adjustments
    val p = cfg[routed] ?: cfg.getValue(ChartType.UNKNOWN)
    return postProcess(dets, p)
  }

  fun activeType(): ChartType = current

  // ---------- internals ----------
  private fun updateEma(t: ChartType) {
    fun step(cur: Float, hit: Boolean) = (1 - alpha) * cur + alpha * (if (hit) 1f else 0f)
    emaCandle = step(emaCandle, t == ChartType.CANDLE)
    emaBar = step(emaBar, t == ChartType.BAR)
    emaLine = step(emaLine, t == ChartType.LINE)
    emaHeikin = step(emaHeikin, t == ChartType.HEIKIN_ASHI)
    emaRenko = step(emaRenko, t == ChartType.RENKO)
  }

  private fun decideType(nowNs: Long): ChartType {
    val scores = listOf(
      ChartType.CANDLE to emaCandle,
      ChartType.BAR to emaBar,
      ChartType.LINE to emaLine,
      ChartType.HEIKIN_ASHI to emaHeikin,
      ChartType.RENKO to emaRenko
    ).sortedByDescending { it.second }

    val (winnerType, winnerScore) = scores[0]
    val runnerUp = scores.getOrNull(1)?.second ?: 0f
    val canSwitch = (nowNs - lastTypeSwitchNs) >= minHoldNs
    val marginOk = (winnerScore - runnerUp) >= hysteresis

    if (current != winnerType && canSwitch && marginOk) {
      current = winnerType
      lastTypeSwitchNs = nowNs
    }
    return current
  }

  private fun postProcess(dets: List<Detection>, p: Params): List<Detection> {
    if (dets.isEmpty()) return dets
    // Adjust confidence by biases (acts like a tiny calibration)
    val tuned = dets.map { d ->
      val wickAdj = p.wickBias
      val bodyAdj = p.bodyBias
      val boost = wickAdj + bodyAdj
      d.copy(confidence = clamp01(d.confidence + boost))
    }
    // Enforce per-type minimum confidence
    return tuned.filter { it.confidence >= p.minConf }
  }

  private fun clamp01(x: Float) = max(0f, min(1f, x))
  private data class Params(val minConf: Float, val wickBias: Float, val bodyBias: Float)
}
```0
