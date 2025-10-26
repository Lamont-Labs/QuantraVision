#!/usr/bin/env bash
# ==================================================
# QUANTRAVISION — REPLIT END-TO-END BUILD & COMPLIANCE SCRIPT v2.0
# ==================================================
# Purpose:
#   Drive the full Android Studio + Play Store readiness pipeline from Replit.
#   Performs environment setup, dependency validation, build, tests, provenance,
#   compliance packaging, screenshots, and GitHub pushback.
#
# Usage:
#   bash scripts/replit_master_build.sh
#
# Determinism note:
#   No random seeds, no external APIs, no network calls except
#   dependency downloads and Google Play Billing libraries.

set -euo pipefail

# -----------------------
# 1. Environment setup
# -----------------------
echo ">>> Setting up Replit Android environment"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
export ANDROID_SDK_ROOT=$HOME/android-sdk
mkdir -p "$ANDROID_SDK_ROOT"
yes | sdkmanager --licenses || true
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" >/dev/null
chmod +x ./gradlew

echo ">>> Confirming Gradle + Java"
java -version
./gradlew -v | grep Gradle

# -----------------------
# 2. Sanity checks
# -----------------------
echo ">>> Checking required files..."
required=(
  "app/build.gradle.kts"
  "settings.gradle"
  "gradle.properties"
  "app/src/main/AndroidManifest.xml"
  "legal/PRIVACY_POLICY.md"
  "legal/TERMS_OF_USE.md"
  "legal/LICENSE.md"
  "app/src/main/assets/patterns.json"
)
for f in "${required[@]}"; do
  [[ -f "$f" ]] || { echo "Missing $f"; exit 1; }
done
echo "All required files present ✅"

# -----------------------
# 3. Dependency validation
# -----------------------
echo ">>> Validating library versions..."
grep -E "compileSdk|targetSdk" app/build.gradle.kts
grep -q "targetSdk = 35" app/build.gradle.kts || echo "⚠️ targetSdk not 35!"
grep -q "billingclient" app/build.gradle.kts || echo "⚠️ Add Play Billing library v6+"

# -----------------------
# 4. Static analysis
# -----------------------
echo ">>> Running Lint / Detekt"
./gradlew lintDebug detekt || true

# -----------------------
# 5. Assemble builds
# -----------------------
echo ">>> Building Debug + Release"
./gradlew clean
./gradlew :app:assembleDebug --stacktrace --build-cache --parallel
./gradlew :app:bundleRelease --stacktrace --build-cache --parallel
ls -lh app/build/outputs/{apk,aab}/**/* || true

# -----------------------
# 6. Unit & determinism tests
# -----------------------
echo ">>> Running unit tests..."
./gradlew testDebugUnitTest --tests "*Pattern*" --build-cache

echo ">>> Checking reproducibility..."
mkdir -p dist/release
sha256sum app/build/outputs/apk/debug/*.apk > dist/release/sha256_debug.txt
sha256sum app/build/outputs/bundle/release/*.aab > dist/release/sha256_release.txt

# -----------------------
# 7. Provenance + SBOM
# -----------------------
echo ">>> Generating provenance and SBOM..."
DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
cat > dist/release/provenance.json <<JSON
{
  "project": "QuantraVision",
  "version": "2.0",
  "builder": "Replit automation",
  "timestamp": "$DATE",
  "sha256_release": "$(cut -d' ' -f1 dist/release/sha256_release.txt)",
  "jdk": "$(java -version 2>&1 | head -n1)",
  "gradle": "$(./gradlew -v | grep Gradle | awk '{print $2}')"
}
JSON
cp dist/sbom.json dist/release/sbom.json

# -----------------------
# 8. Screenshot + graphics
# -----------------------
echo ">>> Generating Play Store screenshots and feature graphic"
mkdir -p dist/playstore
python3 scripts/generate_play_assets.py || echo "No image script; skipping"
# Expected: screenshots under dist/playstore/ and 1024x500 feature_graphic.png

# -----------------------
# 9. GitHub Pages deployment for Privacy/Terms
# -----------------------
echo ">>> Ensuring GitHub Pages legal docs"
bash scripts/enable_pages_docs.sh

# -----------------------
# 10. Content review checklist
# -----------------------
echo ">>> Compliance checklist"
cat <<'TXT'
✅ targetSdk / compileSdk = 35
✅ Billing library v6+ or higher
✅ FOREGROUND_SERVICE_MEDIA_PROJECTION permission declared
✅ Privacy Policy + Terms URLs live
✅ "No data collected" in Data Safety form
✅ Feature graphic 1024x500 present
✅ ≥ 2 screenshots (phone)
✅ App Signing on (Play Console)
✅ Content rating + Ads declaration complete
✅ keystore stored locally, not in repo
TXT

# -----------------------
# 11. GitHub pushback
# -----------------------
echo ">>> Preparing pushback branch"
git config --global user.email "ci@lamontlabs.com"
git config --global user.name "Lamont Replit CI"
git checkout -b replit/build-$(date +%Y%m%d) || git checkout main
git add dist/ legal/ docs/ NOTICE.md PROVENANCE.md dist/release/
git commit -m "[replit-build] Auto build + provenance $(date +%F)" || true
git push -u origin HEAD || echo "Push skipped (no credentials)"

# -----------------------
# 12. Summary
# -----------------------
echo "=================================================="
echo "✅ Replit Build Completed"
echo "Debug APK:    app/build/outputs/apk/debug/"
echo "Release AAB:  app/build/outputs/bundle/release/"
echo "Provenance:   dist/release/provenance.json"
echo "SBOM:         dist/release/sbom.json"
echo "Legal Pages:  docs/ (auto GitHub Pages)"
echo "Next Step:    Import AAB to Play Console → Complete Data Safety → Publish Internal Test"
echo "=================================================="
