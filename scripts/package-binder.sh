#!/usr/bin/env bash
set -euo pipefail
mkdir -p dist
ZIP="dist/QuantraVision_Binder_$(date +%Y%m%d_%H%M%S).zip"
zip -r "$ZIP" \
  README.md app/build/outputs PROVENANCE.md CHANGELOG.md dist/sbom.json \
  app/src/main/assets/patterns >/dev/null
echo "📦 Binder packaged at $ZIP"
