#!/usr/bin/env bash
set -euo pipefail

ROOT="$(pwd)"
SDK_DIR="$HOME/android-sdk"
GRADLE_VER="8.10.2"
AGP_VER="8.7.1"
KOTLIN_VER="1.9.25"
PLATFORM="android-34"
BUILD_TOOLS="34.0.0"

echo "==[ 0/8 ] Environment =="
java -version || true
echo "PWD: $ROOT"

echo "==[ 1/8 ] Android SDK =="
export ANDROID_SDK_ROOT="$SDK_DIR"
export ANDROID_HOME="$SDK_DIR"
mkdir -p "$SDK_DIR"
if ! command -v sdkmanager >/dev/null 2>&1; then
  echo "Installing Android cmdline-tools..."
  mkdir -p "$SDK_DIR/cmdline-tools"
  curl -sSL -o /tmp/cmdtools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q /tmp/cmdtools.zip -d "$SDK_DIR"
  rm -f /tmp/cmdtools.zip
  mkdir -p "$SDK_DIR/cmdline-tools/latest"
  mv "$SDK_DIR/cmdline-tools"/{bin,lib,NOTICE.txt,source.properties} "$SDK_DIR/cmdline-tools/latest/" || true
fi
yes | "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null
"$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;${PLATFORM}" "build-tools;${BUILD_TOOLS}"

echo "==[ 2/8 ] Gradle wrapper ${GRADLE_VER} =="
if [ ! -f "./gradlew" ]; then
  gradle wrapper --gradle-version "${GRADLE_VER}"
  chmod +x ./gradlew
fi
sed -i "s#distributionUrl=.*#distributionUrl=https\\://services.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip#g" \
  gradle/wrapper/gradle-wrapper.properties

echo "==[ 3/8 ] Align AGP/Kotlin plugins =="
# Root plugins (Groovy or Kotlin DSL)
if [ -f "$ROOT/build.gradle" ]; then
  sed -i "s/com.android.application['\: ]\{1,\}version ['\"][0-9.]\+/\0/g" "$ROOT/build.gradle" || true
  perl -0777 -pe "s/(id 'com.android.application' version ')[^']+/\${1}${AGP_VER}/" -i "$ROOT/build.gradle"
  perl -0777 -pe "s/(id 'org.jetbrains.kotlin.android' version ')[^']+/\${1}${KOTLIN_VER}/" -i "$ROOT/build.gradle"
fi
if [ -f "$ROOT/build.gradle.kts" ]; then
  perl -0777 -pe "s/(id\\(\"com.android.application\"\\) version \")([^\"]+)/\${1}${AGP_VER}/" -i "$ROOT/build.gradle.kts"
  perl -0777 -pe "s/(id\\(\"org.jetbrains.kotlin.android\"\\) version \")([^\"]+)/\${1}${KOTLIN_VER}/" -i "$ROOT/build.gradle.kts"
fi

echo "==[ 4/8 ] local.properties sdk.dir =="
echo "sdk.dir=$SDK_DIR" > "$ROOT/local.properties"

echo "==[ 5/8 ] Enable dependency verification =="
mkdir -p "$ROOT/gradle"
SETTINGS_G="$ROOT/settings.gradle"
SETTINGS_KTS="$ROOT/settings.gradle.kts"
if [ -f "$SETTINGS_G" ]; then
  grep -q "dependencyVerification" "$SETTINGS_G" || cat >> "$SETTINGS_G" <<'G'
dependencyVerification {
    verify()
}
G
fi
if [ -f "$SETTINGS_KTS" ]; then
  grep -q "dependencyVerification" "$SETTINGS_KTS" || cat >> "$SETTINGS_KTS" <<'K'
dependencyVerification {
    verify()
}
K
fi
# Generate verification metadata if missing
if [ ! -f "$ROOT/gradle/verification-metadata.xml" ]; then
  ./gradlew --quiet --no-daemon --stacktrace --write-verification-metadata sha256 help || true
fi

echo "==[ 6/8 ] Repro flags and caches =="
# Ensure reproducible packaging flags
GRADLE_PROPS="$ROOT/gradle.properties"
touch "$GRADLE_PROPS"
grep -q "org.gradle.caching=true" "$GRADLE_PROPS" || echo "org.gradle.caching=true" >> "$GRADLE_PROPS"
grep -q "org.gradle.configuration-cache=true" "$GRADLE_PROPS" || echo "org.gradle.configuration-cache=true" >> "$GRADLE_PROPS"
grep -q "android.nonTransitiveRClass=true" "$GRADLE_PROPS" || echo "android.nonTransitiveRClass=true" >> "$GRADLE_PROPS"

echo "==[ 7/8 ] Build, Lint, Test, Bundle =="
./gradlew clean assembleDebug lint test --stacktrace --no-daemon
./gradlew bundleRelease --stacktrace --no-daemon

echo "==[ 8/8 ] Report & hashes =="
OUT="$ROOT/BUILD_REPORT.md"
APK=$(find app/build/outputs/apk/debug -name "*.apk" -maxdepth 1 -print -quit || true)
AAB=$(find app/build/outputs/bundle/release -name "*.aab" -maxdepth 1 -print -quit || true)
echo "# QuantraVision Build Report" > "$OUT"
echo "- Java: $(java -version 2>&1 | head -n1)" >> "$OUT"
echo "- Gradle: ${GRADLE_VER}" >> "$OUT"
echo "- AGP: ${AGP_VER}" >> "$OUT"
echo "- Kotlin: ${KOTLIN_VER}" >> "$OUT"
echo "- SDK: ${PLATFORM} / Build-tools ${BUILD_TOOLS}" >> "$OUT"
if [ -f "$APK" ]; then
  echo "- APK: $APK ($(du -h "$APK" | awk '{print $1}'))" >> "$OUT"
  echo "  SHA256: $(sha256sum "$APK" | awk '{print $1}')" >> "$OUT"
fi
if [ -f "$AAB" ]; then
  echo "- AAB: $AAB ($(du -h "$AAB" | awk '{print $1}'))" >> "$OUT"
  echo "  SHA256: $(sha256sum "$AAB" | awk '{print $1}')" >> "$OUT"
fi
echo "✅ Build complete. See BUILD_REPORT.md"
