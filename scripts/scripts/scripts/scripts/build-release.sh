#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

source ./scripts/ci-env.sh
./scripts/setup-android-sdk.sh

if [[ ! -x "./gradlew" ]]; then
  gradle wrapper --gradle-version 8.10.2
  chmod +x ./gradlew
fi

echo "sdk.dir=$ANDROID_SDK_ROOT" > "$ROOT_DIR/local.properties"

# Build release AAB (signing config must already be set in app/build.gradle.kts)
./gradlew --stacktrace --no-daemon bundleRelease

echo
echo "Release AAB:"
find "$ROOT_DIR/app/build/outputs/bundle/release" -name "*.aab" -maxdepth 1 -print 2>/dev/null || true
