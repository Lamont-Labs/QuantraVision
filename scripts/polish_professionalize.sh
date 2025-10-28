#!/usr/bin/env bash
# QuantraVision — Professional Polish Pipeline v1.0
# Purpose: Apply consistent branding, Material 3 theme, icons, fonts, ProGuard, lint, and docs.
# Run: bash scripts/polish_professionalize.sh
set -euo pipefail

ROOT="$(pwd)"
APP_ID="com.lamontlabs.quantravision"
RES="app/src/main/res"
MAIN="app/src/main"
JAVA_DIR="$MAIN/java/${APP_ID//./\/}"
BRAND_COLOR="#00E5FF"
BRAND_BG="#0A1218"

mkdir -p "$RES/values" "$RES/drawable" "$RES/drawable-anydpi" "$RES/mipmap-anydpi-v26" "$RES/font" "$MAIN/assets" "$JAVA_DIR/ui"

# 1) Colors
cat > "$RES/values/colors.xml" <<EOF
<resources>
    <color name="qv_accent">$BRAND_COLOR</color>
    <color name="qv_bg">$BRAND_BG</color>
    <color name="qv_fg">#E6F7FF</color>
    <color name="qv_card">#14212C</color>
    <color name="qv_warn">#FFB300</color>
    <color name="qv_ok">#2EE6AA</color>
    <color name="qv_err">#FF5252</color>
</resources>
EOF

# 2) Strings
cat > "$RES/values/strings.xml" <<EOF
<resources>
    <string name="app_name">QuantraVision Overlay</string>
    <string name="brand_footer">Lamont Labs</string>
    <string name="watermark_edu">Educational visualization • Not financial advice</string>
</resources>
EOF

# 3) Material 3 Theme (Compose)
cat > "$RES/values/themes.xml" <<'EOF'
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Theme.QuantraVision" parent="Theme.Material3.Dark.NoActionBar">
        <item name="android:statusBarColor">@color/qv_bg</item>
        <item name="android:navigationBarColor">@color/qv_bg</item>
    </style>
</resources>
EOF

# 4) Compose theme Kotlin
cat > "$JAVA_DIR/ui/Theme.kt" <<'EOF'
package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QVColors = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF001318),
    background = Color(0xFF0A1218),
    onBackground = Color(0xFFE6F7FF),
    surface = Color(0xFF14212C),
    onSurface = Color(0xFFE6F7FF),
    error = Color(0xFFFF5252)
)

@Composable
fun QuantraVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = QVColors, typography = Typography(), content = content)
}
EOF

# 5) Watermark drawable
cat > "$RES/drawable/watermark_edu.xml" <<'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="360dp" android:height="24dp" android:viewportWidth="360" android:viewportHeight="24">
    <path android:fillColor="#66000000" android:pathData="M0,0h360v24h-360z"/>
    <path android:fillColor="#FF00E5FF"
        android:pathData="M12,6l4,6 -4,6 -4,-6z"/>
</vector>
EOF

# 6) Adaptive launcher icon
cat > "$RES/mipmap-anydpi-v26/ic_launcher.xml" <<'EOF'
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/qv_bg"/>
    <foreground android:drawable="@drawable/ic_qv_logo"/>
    <monochrome android:drawable="@drawable/ic_qv_logo"/>
</adaptive-icon>
EOF

cat > "$RES/drawable/ic_qv_logo.xml" <<'EOF'
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#00E5FF" android:pathData="M12,54c0,-23.2 18.8,-42 42,-42s42,18.8 42,42 -18.8,42 -42,42 -42,-18.8 -42,-42z"/>
    <path android:fillColor="#0A1218" android:pathData="M30,54c0,-13.3 10.7,-24 24,-24 7.5,0 14.2,3.5 18.5,8.9l-9,6.2c-2.3,-2.9 -5.8,-4.7 -9.7,-4.7 -6.9,0 -12.5,5.6 -12.5,12.5 0,6.9 5.6,12.5 12.5,12.5 3.9,0 7.4,-1.8 9.7,-4.7l9,6.2C68.2,74.5 61.5,78 54,78 40.7,78 30,67.3 30,54z"/>
    <path android:fillColor="#00E5FF" android:pathData="M78,78l18,-12 -6,18z"/>
</vector>
EOF

# 7) ProGuard rules
cat > "$MAIN/proguard-rules.pro" <<'EOF'
# QuantraVision — ProGuard
-dontoptimize
-dontpreverify
-keep class org.tensorflow.** { *; }
-keep class org.opencv.** { *; }
-keep class com.android.billingclient.** { *; }
-keep class androidx.camera.** { *; }
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-assumenosideeffects class android.util.Log { *; }
EOF

# 8) Lint + ktlint (Gradle patch idempotent)
sed -i -E 's/(composeOptions\s*\{[^}]*\})/\1\n  packaging { resources { excludes += ["META-INF\/DEPENDENCIES","META-INF\/INDEX.LIST"] } }/;' app/build.gradle.kts || true
if ! grep -q "com.diffplug.spotless" build.gradle.kts 2>/dev/null; then
  sed -i '1i plugins { id("com.diffplug.spotless") version "6.25.0" apply false }' build.gradle.kts || true
fi
if ! grep -q "spotless" app/build.gradle.kts; then
  cat >> app/build.gradle.kts <<'EOF'

apply(plugin = "com.diffplug.spotless")
spotless {
  kotlin { target("**/*.kt"); ktlint("1.2.1").editorConfigOverride(mapOf("indent_size" to "2","max_line_length" to "140")) }
  format("xml") { target("**/*.xml"); trimTrailingWhitespace(); endWithNewline() }
}
tasks.named("preBuild").configure { dependsOn("spotlessApply") }
EOF
fi

# 9) Settings footer (Lamont Labs), minimal Compose screen if missing
cat > "$JAVA_DIR/ui/SettingsScreen.kt" <<'EOF'
package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    QuantraVisionTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Theme: Follows system (Dark optimized)")
                Text("Overlay opacity: Adjustable in Quick Controls")
                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(12.dp))
                Text("Lamont Labs", color = MaterialTheme.colorScheme.primary)
                Text("QuantraVision Overlay • v2.x")
            }
        }
    }
}
EOF

# 10) README badges auto-update
cat > README.md <<'EOF'
# QuantraVision Overlay

**Offline AI visual assistant for traders.**  
Deterministic overlay. No brokerage APIs. One-time purchase.

![status](https://img.shields.io/badge/build-offline-success)
![android](https://img.shields.io/badge/android-SDK_35-green)
![privacy](https://img.shields.io/badge/privacy-offline-blue)

## Highlights
- 120+ patterns, confidence overlays, MTF confluence
- One-time unlock tiers (Standard / Pro)
- Play Store–ready AAB + screenshots generated in `dist/`
EOF

# 11) Network security + manifest watermark usage (idempotent)
mkdir -p "$RES/xml"
cat > "$RES/xml/network_security_config.xml" <<'EOF'
<network-security-config>
    <base-config cleartextTrafficPermitted="false"/>
</network-security-config>
EOF
sed -i 's|<application|<application android:networkSecurityConfig="@xml/network_security_config"|' "$MAIN/AndroidManifest.xml" || true

# 12) Compose setContent hint file (non-invasive)
if [ ! -f "$JAVA_DIR/ui/AppScaffold.kt" ]; then
cat > "$JAVA_DIR/ui/AppScaffold.kt" <<'EOF'
package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun QuantraVisionApp() {
    QuantraVisionTheme {
        Box(Modifier.fillMaxSize()) {
            // TODO: NavHost + Overlay controls + Detector preview
        }
    }
}
EOF
fi

# 13) Gradient card background drawable
cat > "$RES/drawable/qv_card_bg.xml" <<'EOF'
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="14dp"/>
    <gradient android:startColor="#0E1A22" android:endColor="#14212C" android:angle="90"/>
    <padding android:left="12dp" android:top="12dp" android:right="12dp" android:bottom="12dp"/>
</shape>
EOF

# 14) Detekt optional config (no fail if absent)
if [ ! -f detekt.yml ]; then
cat > detekt.yml <<'EOF'
processors:
  active: true
config:
  validation: true
EOF
fi

echo "== Professional polish applied. Build with ./gradlew :app:assembleDebug or your Replit autobuilder =="
