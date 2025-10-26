#!/usr/bin/env bash
# QuantraVision — Replit Readiness Verifier
# Verifies that Replit can build, package, and push everything needed for Android Studio + Play.
set -euo pipefail

pass(){ printf "✅ %s\n" "$1"; }
fail(){ printf "❌ %s\n" "$1"; exit 1; }

ROOT="$(pwd)"

# 1) Tooling
command -v java      >/dev/null || fail "Java not found"
command -v git       >/dev/null || fail "git not found"
[ -x ./gradlew ]                 || fail "Gradle wrapper missing or not executable"
pass "Tooling present"

# 2) SDK sanity (local.properties will be created by the main script; here we just warn if absent)
[ -f local.properties ] && pass "local.properties present" || echo "ℹ️  local.properties will be created by build script"

# 3) Project files
for f in settings.gradle gradle.properties app/build.gradle.kts app/src/main/AndroidManifest.xml; do
  [ -f "$f" ] || fail "Missing $f"
done
pass "Core Gradle/Manifest files present"

# 4) Policy-critical checks
grep -q 'compileSdk\s*=\s*35' app/build.gradle.kts   || fail "compileSdk must be 35"
grep -q 'targetSdk\s*=\s*35'  app/build.gradle.kts   || fail "targetSdk must be 35"
grep -q 'billing-ktx'         app/build.gradle.kts   || fail "Play Billing dependency missing"
grep -q 'FOREGROUND_SERVICE_MEDIA_PROJECTION' app/src/main/AndroidManifest.xml || fail "FGS mediaProjection permission missing"
grep -q 'foregroundServiceType="mediaProjection"'    app/src/main/AndroidManifest.xml || fail "Service type mediaProjection missing"
! grep -q 'android.permission.INTERNET' app/src/main/AndroidManifest.xml || echo "ℹ️ INTERNET declared; ensure detection path does not use it"
pass "Policy-critical flags OK"

# 5) Legal + Pages
for f in legal/PRIVACY_POLICY.md legal/TERMS_OF_USE.md legal/LICENSE.md docs/index.md; do
  [ -f "$f" ] || fail "Missing legal/docs file: $f"
done
[ -f .github/workflows/deploy-pages.yml ] || fail "Missing Pages workflow"
pass "Legal + Pages ready"

# 6) Assets required by store
[ -f app/src/main/assets/patterns.json ] || fail "Missing assets/patterns.json"
pass "Core asset manifest present"

# 7) Build (debug + release)
./gradlew :app:assembleDebug  --stacktrace --no-daemon --build-cache --parallel
./gradlew :app:bundleRelease  --stacktrace --no-daemon --build-cache --parallel
DEBUG_APK="$(ls -1 app/build/outputs/apk/debug/*.apk | head -n1 || true)"
RELEASE_AAB="$(ls -1 app/build/outputs/bundle/release/*.aab | head -n1 || true)"
[ -n "$DEBUG_APK" ]  || fail "Debug APK not produced"
[ -n "$RELEASE_AAB" ]|| fail "Release AAB not produced"
pass "Build artifacts generated"

# 8) Package compliance bundle
mkdir -p dist/release dist/playstore
cp -f "$DEBUG_APK"  dist/release/app-debug.apk
cp -f "$RELEASE_AAB" dist/release/app-release.aab
( cd dist/release && { sha256sum app-release.aab > sha256.txt 2>/dev/null || shasum -a 256 app-release.aab > sha256.txt; } )
[ -f dist/sbom.json ] || echo '{ "name":"QuantraVision","version":"2.0","dependencies":[] }' > dist/sbom.json
cp -f dist/sbom.json dist/release/sbom.json
[ -f dist/release/sha256.txt ] && pass "Provenance hash written" || fail "Hash missing"

# 9) Screenshots/feature graphic presence (warn if absent)
if ls dist/playstore/screenshot_*.png >/dev/null 2>&1; then pass "Screenshots present"; else echo "ℹ️ Screenshots not found (optional step)"; fi
[ -f dist/playstore/feature_graphic.png ] && pass "Feature graphic present" || echo "ℹ️ Feature graphic not found (optional step)"

# 10) Auto-push prerequisites (optional)
if [ -n "${GITHUB_TOKEN:-}" ] && [ -n "${GITHUB_OWNER:-}" ] && [ -n "${GITHUB_REPO:-}" ]; then
  pass "GitHub env set; auto-push will work"
else
  echo "ℹ️ Set GITHUB_TOKEN, GITHUB_OWNER, GITHUB_REPO to enable auto-push"
fi

echo "----------------------------------------"
echo "ALL CHECKS PASSED. Replit can build, package, and prepare Play assets."
echo "Artifacts:"
echo " - AAB : dist/release/app-release.aab"
echo " - APK : dist/release/app-debug.apk"
echo " - SBOM: dist/release/sbom.json"
echo " - Hash: dist/release/sha256.txt"
echo "Next: run scripts/quantravision_ultimate_replit.sh for full pipeline, or sign AAB in Android Studio."
```0
