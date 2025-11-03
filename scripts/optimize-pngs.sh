#!/bin/bash
# PNG Optimization Script for QuantraVision
# Uses pngcrush for lossless PNG compression

echo "=== PNG Optimization ==="
echo "This script requires pngcrush to be installed"
echo "Install with: sudo apt-get install pngcrush"
echo ""

PNG_DIR="app/src/main/assets/pattern_templates"
TOTAL_BEFORE=0
TOTAL_AFTER=0

if ! command -v pngcrush &> /dev/null; then
    echo "⚠️  pngcrush not found. Install it first."
    echo "For now, showing what would be optimized:"
    find "$PNG_DIR" -name "*.png" -type f | head -10
    exit 1
fi

echo "Optimizing PNGs in $PNG_DIR..."
echo ""

for png in $(find "$PNG_DIR" -name "*.png" -type f); do
    SIZE_BEFORE=$(stat -f%z "$png" 2>/dev/null || stat -c%s "$png" 2>/dev/null)
    TOTAL_BEFORE=$((TOTAL_BEFORE + SIZE_BEFORE))
    
    pngcrush -ow -rem alla -reduce -brute "$png" >/dev/null 2>&1
    
    SIZE_AFTER=$(stat -f%z "$png" 2>/dev/null || stat -c%s "$png" 2>/dev/null)
    TOTAL_AFTER=$((TOTAL_AFTER + SIZE_AFTER))
    
    SAVED=$((SIZE_BEFORE - SIZE_AFTER))
    if [ $SAVED -gt 0 ]; then
        echo "✓ $(basename "$png"): saved ${SAVED} bytes"
    fi
done

TOTAL_SAVED=$((TOTAL_BEFORE - TOTAL_AFTER))
PERCENT_SAVED=$((TOTAL_SAVED * 100 / TOTAL_BEFORE))

echo ""
echo "=== Optimization Complete ==="
echo "Before: $((TOTAL_BEFORE / 1024)) KB"
echo "After:  $((TOTAL_AFTER / 1024)) KB"
echo "Saved:  $((TOTAL_SAVED / 1024)) KB ($PERCENT_SAVED%)"
