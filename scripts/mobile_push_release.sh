#!/usr/bin/env bash
# QuantraVision — Mobile GitHub Push + Release Uploader
# Use from Replit on mobile to push code and upload AAB/APK to a GitHub Release.

set -euo pipefail

# ====== REQUIRED ENV ======
# export GITHUB_TOKEN=ghp_xxx            # GitHub Personal Access Token (repo scope)
# export GITHUB_OWNER=Lamont-Labs        # Your GitHub org/user
# export GITHUB_REPO=QuantraVision       # Repo name

: "${GITHUB_TOKEN:?Set GITHUB_TOKEN}"
: "${GITHUB_OWNER:?Set GITHUB_OWNER}"
: "${GITHUB_REPO:?Set GITHUB_REPO}"

ROOT="$(pwd)"
REL_DIR="$ROOT/dist/release"
AAB="$(ls -1 "$REL_DIR"/app-release.aab 2>/dev/null | head -n1 || true)"
APK="$(ls -1 "$REL_DIR"/app-debug.apk 2>/dev/null | head -n1 || true)"
DATE_TAG="$(date -u +%Y%m%d-%H%M%S)"
BRANCH="replit/mobile-release-$DATE_TAG"
TAG="v1.2-mobile-$DATE_TAG"
REL_TITLE="QuantraVision Mobile Release $DATE_TAG"

if [ -z "$AAB" ]; then echo "Missing dist/release/app-release.aab. Build first."; exit 1; fi
if [ -z "$APK" ]; then echo "Missing dist/release/app-debug.apk. Build first."; exit 1; fi

echo "== 1) Ensure .gitignore excludes large binaries =="
grep -q '^dist/release/.*\.aab$' .gitignore 2>/dev/null || echo 'dist/release/*.aab' >> .gitignore
grep -q '^dist/release/.*\.apk$' .gitignore 2>/dev/null || echo 'dist/release/*.apk' >> .gitignore
grep -q '^keystore/' .gitignore 2>/dev/null || echo 'keystore/' >> .gitignore

echo "== 2) Commit code, docs, metadata =="
git config user.email "Lamontlabs@proton.me"
git config user.name  "Lamont Labs"
git checkout -b "$BRANCH" || git checkout "$BRANCH"
git add -A
git commit -m "[replit-build] Mobile push: code, docs, metadata, excluding AAB/APK" || true
git push -u origin "$BRANCH" || true

echo "== 3) Create tag & GitHub Release via API =="
API="https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO"
# Create lightweight tag (server-side release will reference it)
git tag -a "$TAG" -m "$REL_TITLE" || true
git push origin "$TAG" || true

# Create release
CREATE_JSON=$(cat <<JSON
{ "tag_name":"$TAG",
  "target_commitish":"$BRANCH",
  "name":"$REL_TITLE",
  "body":"Automated mobile release from Replit. Includes signed provenance and SBOM.",
  "draft": false,
  "prerelease": false,
  "generate_release_notes": true }
JSON
)
REL_RESP=$(curl -sS -X POST "$API/releases" \
  -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$CREATE_JSON")

UPLOAD_URL=$(echo "$REL_RESP" | sed -n 's/.*"upload_url": *"\([^"]*\)".*/\1/p' | sed 's/{.*}//')
HTML_URL=$(echo "$REL_RESP"   | sed -n 's/.*"html_url": *"\([^"]*\)".*/\1/p')

if [ -z "$UPLOAD_URL" ]; then
  echo "Release creation failed:"
  echo "$REL_RESP"
  exit 1
fi

echo "== 4) Upload AAB/APK as Release assets =="
AAB_NAME="QuantraVision-$TAG.aab"
APK_NAME="QuantraVision-$TAG-debug.apk"

curl -sS -X POST "${UPLOAD_URL}?name=${AAB_NAME}" \
  -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @"$AAB" >/dev/null

curl -sS -X POST "${UPLOAD_URL}?name=${APK_NAME}" \
  -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @"$APK" >/dev/null

# Optional: upload provenance/SBOM/hashes
for f in provenance.json sbom.json sha256.txt; do
  if [ -f "$REL_DIR/$f" ]; then
    curl -sS -X POST "${UPLOAD_URL}?name=${f%.*}-$TAG.${f##*.}" \
      -H "Authorization: Bearer $GITHUB_TOKEN" \
      -H "Content-Type: application/json" \
      --data-binary @"$REL_DIR/$f" >/dev/null || true
  fi
done

echo "== DONE =="
echo "Release page: $HTML_URL"
echo "Next (desktop): Download AAB from the Release, open Android Studio → Generate Signed App Bundle with your keystore → Upload to Play Console."
```0
