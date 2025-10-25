#!/usr/bin/env bash
set -euo pipefail

root="$(pwd)"
main="$root/app/src/main/java/com/lamontlabs/quantravision"
analysis="$main/analysis"
feat="$root/features"
mkdir -p "$analysis" "$feat"

# Chart type classifier
cat > "$analysis/ChartTypeClassifier.kt" <<'KOT'
package com.lamontlabs.quantravision.analysis
import androidx.camera.core.ImageProxy

enum class ChartType { CANDLE, BAR, LINE, HEIKIN_ASHI, RENKO, UNKNOWN }

interface ChartTypeClassifier {
  fun classify(frame: ImageProxy): ChartType
}
KOT

# Confluence engine
cat > "$analysis/ConfluenceEngine.kt" <<'KOT'
package com.lamontlabs.quantravision.analysis
import com.lamontlabs.quantravision.detection.Detection

data class ConfluenceResult(val score: Float, val agreeingTimeframes: List<String> = emptyList())
interface ConfluenceEngine {
  fun score(detectionsByTf: Map<String, List<Detection>>): ConfluenceResult
}
KOT

# Tradeability engine
cat > "$analysis/TradeabilityEngine.kt" <<'KOT'
package com.lamontlabs.quantravision.analysis
import com.lamontlabs.quantravision.detection.Detection
import kotlin.math.abs
import kotlin.math.min

enum class TradeabilityLabel { NOT_VIABLE, CAUTION, VIABLE }

data class TradeabilityInput(
  val detection: Detection,
  val mtfConfluence: Float?,
  val volRegime: Float,
  val liquidityProxy: Float
)

data class TradeabilityResult(val score: Float, val label: TradeabilityLabel)

class TradeabilityEngine {
  fun evaluate(input: TradeabilityInput): TradeabilityResult {
    val conf = input.detection.confidence.coerceIn(0f,1f)
    val mtf  = (input.mtfConfluence ?: 0.5f).coerceIn(0f,1f)
    val volA = 1f - min(1f, abs(input.volRegime.coerceIn(0f,1f) - 0.5f)*2f)*0.6f
    val liqA = input.liquidityProxy.coerceIn(0f,1f)
    val score = 0.45f*conf + 0.25f*mtf + 0.15f*volA + 0.15f*liqA
    val label = when {
      score < 0.55f -> TradeabilityLabel.NOT_VIABLE
      score < 0.70f -> TradeabilityLabel.CAUTION
      else -> TradeabilityLabel.VIABLE
    }
    return TradeabilityResult(score,label)
  }
}
KOT

# YAML feature docs
mkdir -p "$feat"
cat > "$feat/FeatureSpec_ChartTypeDetection.yaml" <<'YML'
feature_id: "F010"
name: "Chart Type Detection"
version: "1.0"
objective: >
  Identify candlestick, bar, line, Heikin-Ashi, Renko charts and route proper rule set.
status: "Planned"
YML

cat > "$feat/FeatureSpec_MTF_Confluence.yaml" <<'YML'
feature_id: "F011"
name: "MTF Confluence Engine"
version: "1.0"
objective: >
  Validate pattern agreement across multiple timeframes to reduce false positives.
status: "Planned"
YML

cat > "$feat/FeatureSpec_TradeabilityScore.yaml" <<'YML'
feature_id: "F012"
name: "Tradeability Score"
version: "1.0"
objective: >
  Combine confidence, confluence, volatility, and liquidity into a conservative decision aid.
status: "Planned"
YML

# HUD patch
hudfile="$main/overlay/OverlayPainter.kt"
if [ -f "$hudfile" ]; then
  grep -q 'TradeabilityEngine' "$hudfile" || cat >> "$hudfile" <<'PATCH'

// === Tradeability HUD tag ===
import com.lamontlabs.quantravision.analysis.TradeabilityEngine
import com.lamontlabs.quantravision.analysis.TradeabilityInput

private val tradeabilityEngine = TradeabilityEngine()

private fun tradeTag(conf: Float, label: String): String {
  val liq = 0.5f; val vol = 0.5f
  val score = tradeabilityEngine.evaluate(
    TradeabilityInput(
      detection = com.lamontlabs.quantravision.detection.Detection(label,label,android.graphics.RectF(),conf),
      mtfConfluence = 0.5f,
      volRegime = vol,
      liquidityProxy = liq
    )
  )
  return "${label}  ${(conf*100).toInt()}% • ${score.label.name.replace('_',' ')}"
}
PATCH
fi

echo "✅ Reliability upgrade modules applied."
echo "Re-run:  make debug  or  bash scripts/full-build.sh"
