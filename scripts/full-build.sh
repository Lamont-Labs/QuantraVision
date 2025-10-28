#!/usr/bin/env bash
set -euo pipefail
bash ./scripts/build-debug.sh
bash ./scripts/build-release.sh
bash ./scripts/gen-patterns.sh
bash ./scripts/provenance-log.sh
bash ./scripts/sbom.sh
bash ./scripts/changelog.sh
bash ./scripts/package-binder.sh
echo "✅ Full Replit build completed. Ready for Android Studio import."
