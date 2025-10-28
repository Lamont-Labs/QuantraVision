#!/usr/bin/env bash
# QuantraVision — Auto push build artifacts and code back to GitHub from Replit
# Requires env: GITHUB_TOKEN, GITHUB_OWNER, GITHUB_REPO
# Optional env: AUTO_RELEASE=1 to create a GitHub Release with AAB/APK assets

set -euo pipefail

: "${GITHUB_TOKEN:?Set GITHUB_TOKEN}"
: "${GITHUB_OWNER:?Set GITHUB_OWNER}"
: "${GITHUB_REPO:?Set GITHUB_REPO}"

ROOT="$(pwd)"
REL_DIR="$ROOT/dist/release"
PLAY_DIR="$ROOT/dist/playstore"

# Ensure artifacts exist
AAB="$(ls -1 "$REL_DIR"/app-release.aab 2>/dev/null | head -n1 || true)"
APK="$(ls -1 "$REL_DIR"/app-debug.apk 2>/dev/null | head -n1 || true)"
[ -f "$AAB" ] || { echo "Missing AAB at dist/release/app-release.aab"; exit 1; }

DATE_TAG="$(date -u +%Y%m%d-%H%M%S)"
BRANCH="replit/auto-build-$DATE_TAG"
TAG="v1.2-auto-$DATE_TAG"
COMMIT_MSG="[replit-build] Auto build + artifacts $DATE_TAG"

# Git config
git config user.email "ci@lamontlabs.com"
git config user.name  "Lamont Replit CI"

# Ignore secrets and large binaries by default
grep -q '^keystore/' .gitignore 2>/dev/null || echo 'keystore/' >> .gitignore
grep -q '^dist/release/.*\.aab$' .gitignore 2>/dev/null || echo 'dist/release/*.aab' >> .gitignore
grep -q '^dist/release/.*\.apk$' .gitignore 2>/dev/null || echo 'dist/release/*.apk' >> .gitignore

# Commit code and metadata (not AAB/APK)
git checkout -b "$BRANCH" || git checkout "$BRANCH"
git add -A
git commit -m "$COMMIT_MSG" || true
git push -u origin "$BRANCH" || true

# Create tag
git tag -a "$TAG" -m "$COMMIT_MSG" || true
git push origin "$TAG" || true

# Optionally create a GitHub Release and upload binaries
if [ "${AUTO_RELEASE:-0}" = "1" ]; then
  API="https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO"
  CREATE_JSON=$(cat <<JSON
{ "tag_name":"$TAG",
  "target_commitish":"$BRANCH",
  "name":"QuantraVision Auto Release $DATE_TAG",
  "body":"Automated Replit build with provenance and SBOM.",
  "draft": false, "prerelease": false, "generate_release_notes": true }
JSON
)
  REL_RESP=$(curl -sS -X POST "$API/releases" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Content-Type: application/json" \
    -d "$CREATE_JSON")
  UPLOAD_URL=$(echo "$REL_RESP" | sed -n 's/.*"upload_url": *"\([^"]*\)".*/\1/p' | sed 's/{.*}//')
  [ -n "$UPLOAD_URL" ] || { echo "Release creation failed"; echo "$REL_RESP"; exit 1; }

  # Upload assets
  curl -sS -X POST "${UPLOAD_URL}?name=QuantraVision-${TAG}.aab" \
    -H "Authorization: Bearer $GITHUB_TOKEN" \
    -H "Content-Type: application/octet-stream" \
    --data-binary @"$AAB" >/dev/null

  if [ -n "${APK:-}" ] && [ -f "$APK" ]; then
    curl -sS -X POST "${UPLOAD_URL}?name=QuantraVision-${TAG}-debug.apk" \
      -H "Authorization: Bearer $GITHUB_TOKEN" \
      -H "Content-Type: application/octet-stream" \
      --data-binary @"$APK" >/dev/null
  fi

  for f in provenance.json sbom.json sha256.txt; do
    if [ -f "$REL_DIR/$f" ]; then
      curl -sS -X POST "${UPLOAD_URL}?name=${f%.*}-${TAG}.${f##*.}" \
        -H "Authorization: Bearer $GITHUB_TOKEN" \
        -H "Content-Type: application/json" \
        --data-binary @"$REL_DIR/$f" >/dev/null || true
    fi
  done

  # Optional: upload feature graphic and a screenshot
  if [ -f "$PLAY_DIR/feature_graphic.png" ]; then
    curl -sS -X POST "${UPLOAD_URL}?name=feature_graphic-${TAG}.png" \
      -H "Authorization: Bearer $GITHUB_TOKEN" \
      -H "Content-Type: image/png" \
      --data-binary @"$PLAY_DIR/feature_graphic.png" >/dev/null || true
  fi
  ONE_SCREENSHOT="$(ls -1 "$PLAY_DIR"/screenshot_*.png 2>/dev/null | head -n1 || true)"
  if [ -n "$ONE_SCREENSHOT" ]; then
    curl -sS -X POST "${UPLOAD_URL}?name=$(basename "$ONE_SCREENSHOT" .png)-${TAG}.png" \
      -H "Authorization: Bearer $GITHUB_TOKEN" \
      -H "Content-Type: image/png" \
      --data-binary @"$ONE_SCREENSHOT" >/dev/null || true
  fi
fi

echo "Auto push complete."
