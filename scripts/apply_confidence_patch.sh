#!/usr/bin/env bash
set -euo pipefail

root="$(pwd)"
app_dir="$root/app/src/main"
java_dir="$app_dir/java/com/lamontlabs/quantravision"
det_dir="$java_dir/detection"
ovl_dir="$java_dir/overlay"
res_dir="$app_dir/res"
values_dir="$res_dir/values"
assets_dir="$app_dir/assets"
test_dir="$root/app/src/test/java/com/lamontlabs/quantravision/detection"

echo ">> Ensuring directories"
mkdir -p "$det_dir" "$ovl_dir" "$values_dir" "$assets_dir" "$test_dir"

echo ">> Writing detection/Detection.kt"
cat > "$det_dir/Detection.kt" <<'KOT'
package com.lamontlabs.quantravision.detection

import android.graphics.RectF

data class Detection(
  val id: String,
  val label: String,
  val bbox: RectF,       // normalized 0..1 in view coords
  val confidence: Float, // 0f..1f
  val meta: Map<String, Any?> = emptyMap()
)
KOT

echo ">> Writing detection/Detector.kt"
cat > "$det_dir/Detector.kt" <<'KOT'
package com.lamontlabs.quantravision.detection

import android.content.Context
import androidx.camera.core.ImageProxy

interface Detector {
  fun load(context: Context)
  fun analyze(frame: ImageProxy): List<Detection>
}
KOT

echo ">> Writing detection/ConfidenceUtils.kt"
cat > "$det_dir/ConfidenceUtils.kt" <<'KOT'
package com.lamontlabs.quantravision.detection

import android.graphics.Color
import kotlin.math.roundToInt

object ConfidenceUtils {
  fun toPct(c: Float): Int = (c.coerceIn(0f, 1f) * 100f).roundToInt()

  fun colorFor(c: Float): Int {
    val x = c.coerceIn(0f, 1f)
    val r = if (x < 0.5f) (x * 2f * 255).toInt() else 255
    val g = if (x < 0.5f) 255 else ((1f - (x - 0.5f) * 2f) * 255).toInt()
    return Color.argb(192, r, g, 64)
  }

  fun strokeFor(@Suppress("UNUSED_PARAMETER") c: Float): Int = Color.argb(255, 200, 200, 200)
  fun label(det: Detection): String = "${det.label}  ${toPct(det.confidence)}%"
}
KOT

echo ">> Writing overlay/OverlayPainter.kt"
cat > "$ovl_dir/OverlayPainter.kt" <<'KOT'
package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.graphics.*
import android.view.View
import com.lamontlabs.quantravision.detection.ConfidenceUtils
import com.lamontlabs.quantravision.detection.Detection

class OverlayPainter(context: Context) : View(context) {
  private val boxes = mutableListOf<Detection>()
  private val boxPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true }
  private val fillPaint = Paint().apply { style = Paint.Style.FILL }
  private val textPaint = Paint().apply {
    color = Color.WHITE
    textSize = 14f * resources.displayMetrics.scaledDensity
    typeface = Typeface.create("Inter", Typeface.BOLD)
    isAntiAlias = true
  }
  private val bgPaint = Paint().apply { color = Color.argb(180, 0, 0, 0) }

  fun setDetections(d: List<Detection>) {
    boxes.clear(); boxes.addAll(d)
    postInvalidateOnAnimation()
  }

  override fun onDraw(c: Canvas) {
    super.onDraw(c)
    val w = width.toFloat(); val h = height.toFloat()
    boxes.forEach { det ->
      val rect = RectF(det.bbox.left * w, det.bbox.top * h, det.bbox.right * w, det.bbox.bottom * h)
      fillPaint.color = ConfidenceUtils.colorFor(det.confidence)
      c.drawRoundRect(rect, 18f, 18f, fillPaint)
      boxPaint.color = ConfidenceUtils.strokeFor(det.confidence)
      c.drawRoundRect(rect, 18f, 18f, boxPaint)
      val label = ConfidenceUtils.label(det)
      val pad = 8f
      val tw = textPaint.measureText(label)
      val th = textPaint.fontMetrics.run { bottom - top }
      val lb = RectF(rect.left, rect.top - th - 2*pad, rect.left + tw + 2*pad, rect.top)
      c.drawRoundRect(lb, 10f, 10f, bgPaint)
      c.drawText(label, lb.left + pad, lb.bottom - pad*0.6f, textPaint)
    }
  }
}
KOT

echo ">> Writing detection/PatternLibrary.kt"
cat > "$det_dir/PatternLibrary.kt" <<'KOT'
package com.lamontlabs.quantravision.detection

import android.content.Context
import org.json.JSONObject

class PatternLibrary(private val raw: Map<String, PatternDef>) {
  companion object {
    fun load(context: Context): PatternLibrary {
      val txt = context.assets.open("patterns.json").bufferedReader().use { it.readText() }
      val root = JSONObject(txt)
      val map = mutableMapOf<String, PatternDef>()
      root.keys().forEach { id ->
        val o = root.getJSONObject(id)
        val params = o.optJSONObject("params")?.let { p ->
          p.keys().asSequence().associateWith { p.getDouble(it).toFloat() }
        } ?: emptyMap()
        map[id] = PatternDef(id, o.getString("name"), params, o.optDouble("minConfidence", 0.5).toFloat())
      }
      return PatternLibrary(map)
    }
  }

  data class PatternDef(
    val id: String,
    val name: String,
    val params: Map<String, Float>,
    val minConfidence: Float
  )

  fun minConfidence(id: String): Float = raw[id]?.minConfidence ?: 0.5f
}
KOT

echo ">> Writing assets/patterns.json"
cat > "$assets_dir/patterns.json" <<'JSON'
{
  "bull_flag":       { "name": "Bull Flag",         "minConfidence": 0.55, "params": { "minBars": 20 } },
  "head_shoulders":  { "name": "Head & Shoulders",  "minConfidence": 0.60, "params": { "symmetryTol": 0.15 } },
  "double_bottom":   { "name": "Double Bottom",     "minConfidence": 0.55, "params": { "necklineTol": 0.02 } }
}
JSON

echo ">> Ensuring strings.xml has confidence string"
strings="$values_dir/strings.xml"
if [ -f "$strings" ]; then
  grep -q 'confidence_label' "$strings" || \
  sed -i 's#</resources>#  <string name="confidence_label">%1$s  %2$d%%</string>\n</resources>#' "$strings"
else
  cat > "$strings" <<'XML'
<resources>
  <string name="app_name">QuantraVision</string>
  <string name="confidence_label">%1$s  %2$d%%</string>
</resources>
XML
fi

echo ">> Adding unit test"
cat > "$test_dir/ConfidenceFormatTest.kt" <<'KOT'
package com.lamontlabs.quantravision.detection

import org.junit.Assert.assertEquals
import org.junit.Test

class ConfidenceFormatTest {
  @Test fun pct_rounds() {
    assertEquals(55, ConfidenceUtils.toPct(0.547f))
    assertEquals(100, ConfidenceUtils.toPct(1.2f))
    assertEquals(0, ConfidenceUtils.toPct(-0.1f))
  }
}
KOT

echo ">> Patching app/build.gradle(.kts) for assets + tests"
gradlekts="$root/app/build.gradle.kts"
gradlegroovy="$root/app/build.gradle"
if [ -f "$gradlekts" ]; then
  grep -q 'assets.srcDirs' "$gradlekts" || \
  perl -0777 -pe 's/android\s*\{([^}]*)\}/"android {\n$1\n  sourceSets { getByName(\"main\") { assets.srcDirs(\"src/main/assets\") } }\n}"/se' -i "$gradlekts" || true
fi
if [ -f "$gradlegroovy" ]; then
  grep -q 'assets.srcDirs' "$gradlegroovy" || \
  perl -0777 -pe 's/android\s*\{([^}]*)\}/"android {\n$1\n  sourceSets { main { assets.srcDirs = [\"src/main/assets\"] } }\n}"/se' -i "$gradlegroovy" || true
fi

echo ">> Done"
