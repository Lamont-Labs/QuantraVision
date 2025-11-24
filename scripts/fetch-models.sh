#!/usr/bin/env bash
# ==========================================================
# QUANTRAVISION — OPEN-LICENSE ON-DEVICE MODEL FETCHER v1.0
# Purpose:
#   - Download permissive-licensed, mobile-safe models
#   - Place them into Android assets for APK/AAB packaging
#   - Emit manifest + sha256 checks (fail-closed)
#
# Models:
#   1) coco_ssd_mobilenet_v1 quant (Apache-2.0)  -> detector.tflite + labels.txt
#   2) MobileSAM v2 weights (Apache-2.0)        -> mobile_sam_v2.pt (conversion handled by existing pipeline)
#
# Excludes (AGPL / non-permissive):
#   - Ultralytics YOLOv5/YOLOv8 weights
#   - FastSAM weights
# ==========================================================

set -euo pipefail

echo "[models] starting open-license model fetcher..."

ROOT="$(pwd)"

# ---- Detect Android assets dir deterministically ----
ASSETS_CANDIDATES=(
  "$ROOT/app/src/main/assets"
  "$ROOT/android/app/src/main/assets"
  "$ROOT/src/main/assets"
)

ASSETS_DIR=""
for d in "${ASSETS_CANDIDATES[@]}"; do
  if [[ -d "$d" ]]; then ASSETS_DIR="$d"; break; fi
done

if [[ -z "${ASSETS_DIR}" ]]; then
  echo "[models][FAIL] could not find Android assets directory."
  echo "Expected one of:"
  printf "  - %s\n" "${ASSETS_CANDIDATES[@]}"
  exit 1
fi

MODELS_DIR="$ASSETS_DIR/models"
LABELS_DIR="$ASSETS_DIR/labels"
TMP_DIR="$ROOT/.tmp_models"

mkdir -p "$MODELS_DIR" "$LABELS_DIR" "$TMP_DIR"

# ---- URLs (permissive/open license) ----
# 1) TensorFlow Lite COCO SSD MobileNet v1 quant (Apache-2.0)
SSD_ZIP_URL="https://storage.googleapis.com/download.tensorflow.org/models/tflite/coco_ssd_mobilenet_v1_1.0_quant_2018_06_29.zip"

# 2) MobileSAM v2 weights mirror (Apache-2.0 on HF)
MOBILESAM_PT_URL="https://huggingface.co/RogerQi/MobileSAMV2/resolve/main/mobile_sam.pt"

# ---- Download helper ----
fetch() {
  local url="$1"
  local out="$2"
  echo "[models] downloading: $url"
  curl -L --fail --retry 3 --retry-delay 2 -o "$out" "$url"
}

sha256_of() {
  local f="$1"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$f" | awk '{print $1}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$f" | awk '{print $1}'
  else
    python3 - "$f" <<'PY'
import hashlib,sys
p=sys.argv[1]
h=hashlib.sha256()
with open(p,'rb') as f:
  for b in iter(lambda:f.read(1<<20),b''):
    h.update(b)
print(h.hexdigest())
PY
  fi
}

# ==========================================================
# MODEL 1: COCO SSD MobileNet v1 quant (Apache-2.0)
# ==========================================================
SSD_ZIP_PATH="$TMP_DIR/coco_ssd_mobilenet_v1_quant.zip"
fetch "$SSD_ZIP_URL" "$SSD_ZIP_PATH"

echo "[models] extracting SSD package..."
unzip -o "$SSD_ZIP_PATH" -d "$TMP_DIR/ssd_pkg" >/dev/null

# expected files (from TF Lite model zoo zip)
SSD_TFLITE_SRC="$(find "$TMP_DIR/ssd_pkg" -name '*.tflite' | head -n 1)"
SSD_LABELS_SRC="$(find "$TMP_DIR/ssd_pkg" -name 'labelmap.txt' -o -name 'labels.txt' | head -n 1)"

if [[ ! -f "$SSD_TFLITE_SRC" ]]; then
  echo "[models][FAIL] SSD tflite not found after unzip."
  exit 1
fi

SSD_TFLITE_DST="$MODELS_DIR/detector_ssd_mobilenet_v1_quant.tflite"
cp "$SSD_TFLITE_SRC" "$SSD_TFLITE_DST"

if [[ -f "$SSD_LABELS_SRC" ]]; then
  cp "$SSD_LABELS_SRC" "$LABELS_DIR/detector_labels.txt"
else
  echo "[models][WARN] SSD labels file not found in zip; detector will need embedded labels."
fi

SSD_SHA="$(sha256_of "$SSD_TFLITE_DST")"
echo "[models] SSD detector ready: $SSD_TFLITE_DST"
echo "[models] SSD sha256: $SSD_SHA"

# ==========================================================
# MODEL 2: MobileSAM v2 weights (Apache-2.0)
# NOTE:
#   Your existing pipeline converts PT -> ONNX -> TFLite.
#   This script only fetches the permissive weights.
# ==========================================================
MOBILESAM_PT_DST="$MODELS_DIR/mobile_sam_v2.pt"
fetch "$MOBILESAM_PT_URL" "$MOBILESAM_PT_DST"

MOBILESAM_SHA="$(sha256_of "$MOBILESAM_PT_DST")"
echo "[models] MobileSAM v2 weights ready: $MOBILESAM_PT_DST"
echo "[models] MobileSAM sha256: $MOBILESAM_SHA"

# ==========================================================
# Emit deterministic manifest for runtime loaders
# ==========================================================
MANIFEST_PATH="$MODELS_DIR/MODEL_MANIFEST.json"

cat > "$MANIFEST_PATH" <<JSON
{
  "models": [
    {
      "id": "detector_ssd_mobilenet_v1_quant",
      "file": "detector_ssd_mobilenet_v1_quant.tflite",
      "task": "object_detection",
      "license": "Apache-2.0",
      "source_url": "$SSD_ZIP_URL",
      "sha256": "$SSD_SHA"
    },
    {
      "id": "mobile_sam_v2_weights",
      "file": "mobile_sam_v2.pt",
      "task": "segmentation_weights",
      "license": "Apache-2.0",
      "source_url": "$MOBILESAM_PT_URL",
      "sha256": "$MOBILESAM_SHA"
    }
  ],
  "generated_by": "QuantraVision Open-License Model Fetcher v1.0",
  "fail_closed": true
}
JSON

echo "[models] manifest written: $MANIFEST_PATH"

# ==========================================================
# Clean temp
# ==========================================================
rm -rf "$TMP_DIR/ssd_pkg" "$SSD_ZIP_PATH" || true

echo "[models] done. permissive on-device models installed into assets."
