#!/usr/bin/env bash
set -euo pipefail
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0")
echo "# Changelog — $(date -u)" > CHANGELOG.md
git log ${LAST_TAG}..HEAD --oneline >> CHANGELOG.md
