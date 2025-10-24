#!/usr/bin/env bash
set -euo pipefail

# Android SDK locations inside Replit
export ANDROID_SDK_ROOT="$HOME/android-sdk"
export ANDROID_HOME="$ANDROID_SDK_ROOT"

# Build-tools / platform you target
export ANDROID_PLATFORM="android-34"
export ANDROID_BUILD_TOOLS="34.0.0"

# Java
export JAVA_HOME="${JAVA_HOME:-/nix/store/*-jdk-17*/lib/openjdk}"
export PATH="$JAVA_HOME/bin:$PATH"

# SDK tools on PATH when installed
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
