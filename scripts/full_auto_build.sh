#!/usr/bin/env bash
set -euo pipefail
root="$(pwd)"
echo "==== QuantraVision Full Auto Build ===="

bash ./scripts/setup-android-sdk.sh
[[ -x ./gradlew ]] || gradle wrapper --gradle-version 8.10.2 && chmod +x ./gradlew
echo "sdk.dir=$HOME/android-sdk" > "$root/local.properties"

# New: apply enterprise hardening + confidence + reliability packs if present
[ -x ./scripts/apply_enterprise_hardening.sh ] && bash ./scripts/apply_enterprise_hardening.sh || true
[ -x ./scripts/apply_confidence_patch.sh ] && bash ./scripts/apply_confidence_patch.sh || true
[ -x ./scripts/apply_trader_reliability_upgrades.sh ] && bash ./scripts/apply_trader_reliability_upgrades.sh || true

./gradlew clean assembleDebug assembleRelease --stacktrace --no-daemon

bash ./scripts/gen-patterns.sh || true
bash ./scripts/gen_playstore_images.sh || true
bash ./scripts/provenance-log.sh || true
bash ./scripts/sbom.sh || true
bash ./scripts/changelog.sh || true
bash ./scripts/package-binder.sh || true
bash ./scripts/quality_gate.sh || true

if [[ -n "${GITHUB_TOKEN:-}" ]]; then
  bash ./scripts/upload-artifacts.sh || true
fi

echo "==== Build Complete ===="
find app/build/outputs -type f \( -name '*.apk' -o -name '*.aab' \) -print
echo "Artifacts, binder, screenshots in /dist/"
