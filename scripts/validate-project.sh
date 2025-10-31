#!/bin/bash
# QuantraVision Project Validation Script
# Validates repository structure and build configuration

echo "================================================"
echo "QuantraVision Project Validation"
echo "================================================"
echo ""

ERRORS=0

echo "✓ Checking project structure..."
if [ -d "app/src/main/java/com/lamontlabs/quantravision" ]; then
    echo "  ✓ Source code directory exists"
else
    echo "  ✗ Source code directory missing"
    ERRORS=$((ERRORS + 1))
fi

if [ -d "app/src/main/res" ]; then
    echo "  ✓ Resources directory exists"
else
    echo "  ✗ Resources directory missing"
    ERRORS=$((ERRORS + 1))
fi

if [ -d "app/src/main/assets" ]; then
    echo "  ✓ Assets directory exists"
else
    echo "  ✗ Assets directory missing"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "✓ Checking build configuration..."
if [ -f "build.gradle.kts" ]; then
    echo "  ✓ Root build.gradle.kts exists"
else
    echo "  ✗ Root build.gradle.kts missing"
    ERRORS=$((ERRORS + 1))
fi

if [ -f "app/build.gradle.kts" ]; then
    echo "  ✓ App build.gradle.kts exists"
else
    echo "  ✗ App build.gradle.kts missing"
    ERRORS=$((ERRORS + 1))
fi

if [ -f "settings.gradle.kts" ]; then
    echo "  ✓ settings.gradle.kts exists"
else
    echo "  ✗ settings.gradle.kts missing"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "✓ Checking documentation..."
if [ -f "README.md" ]; then
    echo "  ✓ README.md exists"
else
    echo "  ✗ README.md missing"
    ERRORS=$((ERRORS + 1))
fi

if [ -f "QUICK_START.md" ]; then
    echo "  ✓ QUICK_START.md exists"
else
    echo "  ✗ QUICK_START.md missing"
    ERRORS=$((ERRORS + 1))
fi

if [ -f "LICENSE" ]; then
    echo "  ✓ LICENSE exists"
else
    echo "  ✗ LICENSE missing"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "✓ Checking essential assets..."
PATTERN_COUNT=$(find app/src/main/assets/pattern_templates -name "*.yaml" 2>/dev/null | wc -l)
echo "  ✓ Found $PATTERN_COUNT pattern templates"

if [ -f "app/src/main/res/drawable/lamont_labs_logo.png" ]; then
    echo "  ✓ Lamont Labs logo exists"
else
    echo "  ✗ Lamont Labs logo missing"
    ERRORS=$((ERRORS + 1))
fi

if [ -f "app/src/main/res/drawable/quantravision_logo.png" ]; then
    echo "  ✓ QuantraVision logo exists"
else
    echo "  ✗ QuantraVision logo missing"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "================================================"
if [ $ERRORS -eq 0 ]; then
    echo "✅ Project validation PASSED"
    echo "Repository is ready for Android Studio build"
    echo ""
    echo "Next steps:"
    echo "  1. Open project in Android Studio Ladybug (2024.2.1+)"
    echo "  2. Let Gradle sync complete"
    echo "  3. Build APK with: ./gradlew assembleDebug"
else
    echo "❌ Project validation FAILED with $ERRORS errors"
    echo "Please fix the issues above before building"
fi
echo "================================================"
