#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

source ./scripts/ci-env.sh
./scripts/setup-android-sdk.sh

# Ensure Gradle wrapper exists; generate with system Gradle if missing
if [[ ! -x "./gradlew" ]]; then
  echo "Generating Gradle wrapper..."
  gradle wrapper --gradle-version 8.10.2
  chmod +x ./gradlew
fi

# Ensure local.properties points to our SDK
echo "sdk.dir=$ANDROID_SDK_ROOT" > "$ROOT_DIR/local.properties"

# Build debug APK only (skip lint and tests for now)
./gradlew --stacktrace --no-daemon assembleDebug

# Report artifact locations
echo
echo "Debug APK:"
find "$ROOT_DIR/app/build/outputs/apk/debug" -name "*.apk" -maxdepth 1 -print 2>/dev/null || true
