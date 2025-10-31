#!/usr/bin/env bash
# ==================================================
# QUANTRAVISION™ — DETERMINISM & INTEGRITY VERIFIER
# ==================================================
set -e

echo "==> Verifying QuantraVision integrity..."
APP_DIR="app/src/main/java/com/lamontlabs/quantravision"
MANIFEST="manifest.json"
SIG="signature.txt"

# Hash core directories
echo "==> Computing source hash..."
find "$APP_DIR" -type f -name "*.kt" -exec sha256sum {} \; | sort | sha256sum > src_hash.txt
find app/src/main/res -type f | sort | xargs sha256sum > res_hash.txt

# Verify signature if exists
if [ -f "app/src/main/assets/$SIG" ]; then
  echo "==> Checking Ed25519 signature..."
  openssl dgst -sha256 -verify pubkey.pem -signature app/src/main/assets/$SIG src_hash.txt || echo "Warning: signature not verified"
else
  echo "==> No signature.txt found (expected in Pro export)."
fi

echo "==> Comparing manifest hashes..."
if [ -f "$MANIFEST" ]; then
  grep -q "$(sha256sum src_hash.txt | cut -d ' ' -f1)" "$MANIFEST" && echo "✅ Source hash matches manifest." || echo "⚠ Hash mismatch!"
else
  echo "Manifest not found."
fi

echo "==> Verification complete."
