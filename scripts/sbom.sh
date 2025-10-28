#!/usr/bin/env bash
set -euo pipefail
mkdir -p dist
cyclonedx-bom -o dist/sbom.json -e app
echo "✅ SBOM created at dist/sbom.json"
