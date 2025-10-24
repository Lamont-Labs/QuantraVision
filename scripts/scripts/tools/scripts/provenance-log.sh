#!/usr/bin/env bash
set -euo pipefail
OUT="PROVENANCE.md"
echo "# Build Provenance — $(date -u)" > "$OUT"
echo "" >> "$OUT"
echo "## Environment" >> "$OUT"
echo "- Gradle: $(gradle -v | head -n 1)" >> "$OUT"
echo "- JDK: $(java -version 2>&1 | head -n 1)" >> "$OUT"
echo "- Android Build Tools: $ANDROID_BUILD_TOOLS" >> "$OUT"
echo "" >> "$OUT"
echo "## Artifacts" >> "$OUT"
find app/build/outputs -type f \( -name "*.apk" -o -name "*.aab" \) \
  -exec shasum -a 256 {} \; >> "$OUT"
