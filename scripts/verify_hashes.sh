#!/bin/sh
# Deterministic integrity check for QuantraVision Overlay assets.

echo "Verifying SHA-256 hashes for pattern templates and demo charts..."

for f in app/assets/pattern_templates/*.yaml; do
  sha256sum "$f"
done

for f in app/assets/demo_charts/*; do
  sha256sum "$f"
done

echo "Verification complete. Compare output with PROVENANCE.md expected hashes."
