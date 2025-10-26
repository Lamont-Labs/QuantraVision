#!/usr/bin/env bash
# QuantraVision — Add Offline Feature Modules (Replit helper)
# Creates Replay Studio, Strategy Scorer, Explain Pane, Export Bundle,
# Pattern Editor stubs, OCR reader stub, Thermal/Battery guard, FirstRunConsent,
# Synthetic generator test, onboarding assets, and directories.
#
# Usage: bash scripts/add_offline_features.sh
# Idempotent: will not overwrite existing files unless --force is passed.
#
# Provenance header inserted into each created source file.
# Files are minimal, compile-friendly stubs to integrate with the existing app.
#
set -euo pipefail

ROOT="$(pwd)"
FORCE=0
if [ "${1:-}" = "--force" ]; then FORCE=1; fi

PH="/*\n * Added by Replit task: add_offline_features\n * Reason: add offline decision-support modules (replay, scorer, editor)\n * Determinism: pure local files, no RNG, no network in detection path\n * Date: $(date -u +%Y-%m-%d)\n */\n\n"

mkfile() {
  local path="$1"; local body="$2"
  if [ -f "$path" ] && [ "$FORCE" -eq 0 ]; then
    echo "Skipping existing $path"
    return
  fi
  mkdir -p "$(dirname "$path")"
  printf "%b%s" "$PH" "$body" > "$path"
  echo "Wrote $path"
}

echo "Creating modules..."

# 1) SessionPlayback.kt (Replay Studio)
mkfile "app/src/main/java/com/lamontlabs/quantravision/replay/SessionPlayback.kt" \
"package com.lamontlabs.quantravision.replay

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class ReplayFrame(val timestamp: Long, val imagePath: String)

class SessionPlayback(private val ctx: Context) {
  private val frames = mutableListOf<ReplayFrame>()
  fun loadFromFolder(folderPath: String) {
    // Minimal loader: list png/jpg files in folderPath sorted by name.
    // Integrate with app's storage APIs for robustness.
  }
  fun play(onFrame: (ReplayFrame) -> Unit, fps: Int = 4) {
    CoroutineScope(Dispatchers.Default).launch {
      val delayMs = (1000 / fps.toLong()).coerceAtLeast(100L)
      for (f in frames) {
        onFrame(f)
        delay(delayMs)
      }
    }
  }
  fun addFrame(f: ReplayFrame) { frames.add(f) }
  fun clear() { frames.clear() }
}
"

# 2) Replay UI (Compose stub)
mkfile "app/src/main/java/com/lamontlabs/quantravision/ui/ReplayScreen.kt" \
"package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.lamontlabs.quantravision.replay.SessionPlayback

@Composable
fun ReplayScreen() {
  val ctx = LocalContext.current
  var playing by remember { mutableStateOf(false) }
  Column(Modifier.fillMaxSize().padding(12.dp)) {
    Text(\"Replay Studio\", Modifier.padding(4.dp))
    Button(onClick = { playing = !playing }) { Text(if (playing) \"Stop\" else \"Play\") }
    Spacer(Modifier.height(12.dp))
    // Placeholder: image area
    Box(Modifier.fillMaxWidth().height(300.dp)) {
      AsyncImage(model = \"\", contentDescription = \"replay-frame\")
    }
  }
}
"

# 3) StrategyScorer.kt (scoring engine)
mkfile "app/src/main/java/com/lamontlabs/quantravision/analysis/StrategyScorer.kt" \
"package com.lamontlabs.quantravision.analysis

data class ScoreResult(val score: Int, val reasons: List<String>, val caution: String)

object StrategyScorer {
  // Deterministic scoring combining confidence, MTF confluence and risk
  // Input is a simple map of signals and floats; expand with concrete types.
  fun score(signals: Map<String, Float>, risk: Float): ScoreResult {
    val base = (signals.values.sum() / (signals.size.coerceAtLeast(1))).coerceIn(0f,1f)
    val raw = (base * 100 * (1f - risk)).toInt().coerceIn(0,100)
    val reasons = signals.map { (k,v) -> \"$k=${(v*100).toInt()}%\" }
    val caution = when {
      risk > 0.66f -> \"High risk — volatile/low liquidity\"
      risk > 0.33f -> \"Elevated risk — check MTF confluence\"
      else -> \"Normal market conditions\"
    }
    return ScoreResult(raw, reasons, caution)
  }
}
"

# 4) ExplainPane.kt (explainability UI)
mkfile "app/src/main/java/com/lamontlabs/quantravision/ui/ExplainPane.kt" \
"package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ExplainPane(title: String, lines: List<String>) {
  Card {
    Column {
      Text(title)
      for (l in lines) Text(\"• $l\")
    }
  }
}
"

# 5) ExportBundleBuilder.kt (export proof bundle)
mkfile "app/src/main/java/com/lamontlabs/quantravision/export/ExportBundleBuilder.kt" \
"package com.lamontlabs.quantravision.export

import android.content.Context
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExportBundleBuilder {
  fun buildProofZip(ctx: Context, srcDir: File, outZip: File) {
    ZipOutputStream(outZip.outputStream()).use { zos ->
      srcDir.walkTopDown().filter { it.isFile }.forEach { f ->
        val entry = ZipEntry(f.relativeTo(srcDir).path)
        zos.putNextEntry(entry)
        f.inputStream().copyTo(zos)
        zos.closeEntry()
      }
    }
  }
}
"

# 6) PatternEditorActivity.kt (user templates)
mkfile "app/src/main/java/com/lamontlabs/quantravision/editor/PatternEditorActivity.kt" \
"package com.lamontlabs.quantravision.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class PatternEditorActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { EditorScreen() }
  }
}

@Composable
fun EditorScreen() {
  Text(\"Pattern Editor — draw a template and save locally.\")
}
"

# 7) OCRReader.kt (stub)
mkfile "app/src/main/java/com/lamontlabs/quantravision/analysis/OCRReader.kt" \
"package com.lamontlabs.quantravision.analysis

import android.content.Context
import android.graphics.Bitmap

object OCRReader {
  // Lightweight heuristics or integrate an on-device OCR model.
  fun readPriceFromBitmap(bmp: Bitmap): String? {
    // Stub: return null when OCR not available.
    return null
  }
}
"

# 8) ThermalBatteryGuard.kt
mkfile "app/src/main/java/com/lamontlabs/quantravision/system/ThermalBatteryGuard.kt" \
"package com.lamontlabs.quantravision.system

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi

object ThermalBatteryGuard {
  fun shouldThrottle(context: Context): Boolean {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    return level in 0..15
  }
}
"

# 9) FirstRunConsent.kt (consent flow)
mkfile "app/src/main/java/com/lamontlabs/quantravision/ui/FirstRunConsent.kt" \
"package com.lamontlabs.quantravision.ui

import android.content.Context
import android.content.SharedPreferences

object FirstRunConsent {
  private const val PREF = \"qv_prefs\"
  private const val KEY_ACCEPTED = \"first_run_accepted\"
  fun isAccepted(ctx: Context): Boolean {
    val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    return p.getBoolean(KEY_ACCEPTED, false)
  }
  fun accept(ctx: Context) {
    ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean(KEY_ACCEPTED, true).apply()
  }
}
"

# 10) SyntheticGenerator.kt (test utility)
mkfile "app/src/test/java/com/lamontlabs/quantravision/test/SyntheticGenerator.kt" \
"package com.lamontlabs.quantravision.test

import org.junit.Test
import kotlin.test.assertTrue
import java.io.File

class SyntheticGenerator {
  @Test
  fun generateBasic() {
    val out = File(\"src/test/resources/golden/synthetic_stub.txt\")
    out.parentFile.mkdirs()
    out.writeText(\"synthetic-ok\")
    assertTrue(out.exists())
  }
}
"

# 11) Onboarding assets and directory
mkdir -p app/src/main/assets/onboarding
if [ ! -f app/src/main/assets/onboarding/intro.txt ]; then
  echo \"Welcome to QuantraVision. Open this app before your trading app. This overlay observes only.\" > app/src/main/assets/onboarding/intro.txt
  echo \"Wrote onboarding intro text.\" 
fi

# 12) Add assets/custom_patterns placeholder
mkdir -p app/src/main/assets/custom_patterns
touch app/src/main/assets/custom_patterns/.gitkeep

# 13) Update NOTICE.md (append entry)
if [ -f NOTICE.md ]; then
  grep -q 'Offline features modules' NOTICE.md || printf \"\n- Offline features modules: Replay Studio, Strategy Scorer, Pattern Editor (added by Replit helper)\" >> NOTICE.md
else
  echo \"- Offline features modules: Replay Studio, Strategy Scorer, Pattern Editor (added by Replit helper)\" > NOTICE.md
fi
echo \"NOTICE.md updated.\"

# 14) Add simple menu route (MainActivity integration note)
echo \"\n// NOTE: Import and wire new screens (ReplayScreen, ExplainPane, Editor) into your MainActivity navigation.\" > tmp_integration_note.txt
mv -f tmp_integration_note.txt app/src/main/java/com/lamontlabs/quantravision/INTEGRATION_NOTES.txt

# 15) Make script executable
chmod +x scripts/add_offline_features.sh

echo \"All offline feature stubs created. Next: open project in Android Studio or run Replit build to compile.\"
echo \"If you want full implementations for any file, run with --force to overwrite or request specific files next.\"
