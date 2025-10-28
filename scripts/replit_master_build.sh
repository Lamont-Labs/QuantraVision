#!/usr/bin/env bash
set -euo pipefail
ROOT="$(pwd)"
SDK="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$SDK"
export ANDROID_HOME="$SDK"

echo "======== QuantraVision Master Build ========"

# 1. Install SDK + Tools
bash ./scripts/harden_build_for_release.sh

# 2. Enforce offline mode
bash ./scripts/enforce_offline_mode.sh

# 3. Apply enterprise hardening + reliability + compliance guard
bash ./scripts/apply_enterprise_hardening.sh
bash ./scripts/apply_trader_reliability_upgrades.sh || true

# 4. Generate visuals
bash ./scripts/gen_playstore_images.sh || true

# 5. Lint, build, and test
./gradlew clean ktlintCheck detekt assembleDebug test --stacktrace --no-daemon
./gradlew bundleRelease --stacktrace --no-daemon

# 6. Auto-generate localized screenshots
python3 <<'PY'
import os, shutil
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

root = Path("dist/playstore/locale")
langs = {"en":"See patterns instantly","de":"Erkenne Muster sofort","fr":"Détecte les motifs instantanément","ja":"パターンを即座に検出"}
font = ImageFont.load_default()
for code, text in langs.items():
    d = root/code
    d.mkdir(parents=True, exist_ok=True)
    base = Image.open("dist/playstore/screenshot_1.png").convert("RGB")
    draw = ImageDraw.Draw(base)
    draw.rectangle([0,0,1080,180],fill=(10,15,20,255))
    draw.text((40,80), text, fill=(0,229,255), font=font)
    base.filter(ImageFilter.SMOOTH_MORE).save(d/"screen_1.png")
PY

# 7. Provenance + binder pack
echo "== provenance =="
mkdir -p dist/binder
sha256sum app/build/outputs/apk/debug/app-debug.apk > dist/binder/hash.txt
date +"%Y-%m-%dT%H:%M:%S" >> dist/binder/hash.txt
zip -r dist/binder/QuantraVision_Binder_$(date +%Y%m%d).zip \
  app/build/outputs/apk/debug/app-debug.apk \
  app/build/outputs/bundle/release/*.aab \
  dist/playstore/ \
  PROVENANCE.md \
  BUILD_REPORT.md \
  dist/binder/hash.txt || true

echo "== DONE =="
find dist -maxdepth 2 -type f \( -name '*.apk' -o -name '*.aab' -o -name '*.png' -o -name '*.zip' \)
echo "Artifacts ready in /dist/"
