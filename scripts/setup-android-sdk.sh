#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/ci-env.sh"

# Skip if already provisioned
if [[ -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]]; then
  echo "Android SDK already present at $ANDROID_SDK_ROOT"
  exit 0
fi

mkdir -p "$ANDROID_SDK_ROOT"
cd "$ANDROID_SDK_ROOT"

# Get Google commandline-tools and place under cmdline-tools/latest
echo "Downloading Android commandline-tools..."
curl -L -o cmdline-tools.zip \
  https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

unzip -q cmdline-tools.zip -d "$ANDROID_SDK_ROOT"
rm -f cmdline-tools.zip

# The zip extracts to 'cmdline-tools' with bin, lib, etc inside
# We need to move it to 'cmdline-tools/latest/'
mv "$ANDROID_SDK_ROOT/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools-temp"
mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools/latest"
mv "$ANDROID_SDK_ROOT/cmdline-tools-temp"/* "$ANDROID_SDK_ROOT/cmdline-tools/latest/"
rm -rf "$ANDROID_SDK_ROOT/cmdline-tools-temp"

# Accept licenses and install required packages
yes | "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null

"$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" \
  "platform-tools" \
  "platforms;${ANDROID_PLATFORM}" \
  "build-tools;${ANDROID_BUILD_TOOLS}"
