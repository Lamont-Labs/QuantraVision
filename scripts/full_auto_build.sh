#!/usr/bin/env bash
set -euo pipefail
root="$(pwd)"
echo "==== QuantraVision Full Auto Build ===="

# 1. SDK and Gradle environment
bash ./scripts/setup-android-sdk.sh

# 2. Generate Gradle wrapper if missing
[[ -x ./gradlew ]] || gradle wrapper --gradle-version 8.10.2 && chmod +x ./gradlew

# 3. Ensure local.properties
echo "sdk.dir=$HOME/android-sdk" > "$root/local.properties"

# 4. Build debug + release
./gradlew clean assembleDebug assembleRelease --stacktrace --no-daemon

# 5. Generate all pattern + marketing assets
bash ./scripts/gen-patterns.sh
bash ./scripts/gen_playstore_images.sh

# 6. Provenance, SBOM, changelog
bash ./scripts/provenance-log.sh
bash ./scripts/sbom.sh
bash ./scripts/changelog.sh

# 7. Binder packaging
bash ./scripts/package-binder.sh

# 8. Optional upload to GitHub if GITHUB_TOKEN is set
if [[ -n "${GITHUB_TOKEN:-}" ]]; then
  bash ./scripts/upload-artifacts.sh || true
fi

echo
echo "==== Build Complete ===="
find app/build/outputs -type f \( -name '*.apk' -o -name '*.aab' \) -print
echo "Artifacts, binder, provenance, and Play Store screenshots are in /dist/"
