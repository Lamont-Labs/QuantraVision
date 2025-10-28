#!/usr/bin/env bash
set -euo pipefail
# Requires GITHUB_TOKEN in Replit secrets
if [ -z "${GITHUB_TOKEN:-}" ]; then
  echo "❌ Missing GITHUB_TOKEN in Secrets"
  exit 1
fi

REPO="Lamont-Labs/QuantraVision"
TAG="build-$(date +%Y%m%d-%H%M%S)"
gh release create "$TAG" dist/*.zip dist/*.apk dist/*.aab -R "$REPO" -t "$TAG" -n "Automated Replit build"
