#!/bin/bash
set -e

echo "🔍 Running local Kotlin compilation check..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

cd app

# Run Kotlin compilation only (fast, no APK generation)
./gradlew compileDebugKotlin --no-daemon --console=plain 2>&1 | tee /tmp/compile_output.txt

# Check for errors
if grep -q "^e: " /tmp/compile_output.txt; then
    echo ""
    echo "❌ COMPILATION ERRORS FOUND:"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    grep "^e: " /tmp/compile_output.txt
    echo ""
    echo "Fix these errors before pushing to avoid CI failures."
    exit 1
fi

echo ""
echo "✅ Compilation successful - safe to push!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
