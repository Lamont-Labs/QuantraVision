#!/bin/sh
# Exports local provenance log into Greyline OS compatible YAML bundle.

SRC="app/src/main/assets"
LOGFILE="app/src/main/files/provenance.log"
OUT="dist/provenance_bundle.yaml"

mkdir -p dist
echo "export_date: $(date -u '+%Y-%m-%dT%H:%M:%SZ')" > "$OUT"
echo "entries:" >> "$OUT"

awk '{print "  - " $0}' "$LOGFILE" >> "$OUT"

echo "Provenance bundle created at $OUT"
