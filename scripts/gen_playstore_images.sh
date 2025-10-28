#!/usr/bin/env bash
set -euo pipefail
root="$(pwd)"
out="$root/dist/playstore"
mkdir -p "$out"

python3 scripts/tools/gen_playstore_images.py
echo "✅ Play Store screenshots created in $out"
