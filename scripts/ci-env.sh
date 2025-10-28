#!/usr/bin/env bash
set -euo pipefail

# Android SDK locations inside Replit
export ANDROID_SDK_ROOT="$HOME/android-sdk"
export ANDROID_HOME="$ANDROID_SDK_ROOT"

# Build-tools / platform you target
export ANDROID_PLATFORM="android-34"
export ANDROID_BUILD_TOOLS="34.0.0"

# Java - auto-detect from the java binary in PATH
if [ -z "${JAVA_HOME:-}" ]; then
  JAVA_BIN=$(which java 2>/dev/null)
  if [ -n "$JAVA_BIN" ]; then
    export JAVA_HOME=$(readlink -f "$JAVA_BIN" | sed 's|/bin/java||')
  fi
fi
export PATH="${JAVA_HOME}/bin:$PATH"

# SDK tools on PATH when installed
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
