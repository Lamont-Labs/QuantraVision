# GitHub Actions CI/CD

This repository uses GitHub Actions to build Android APKs automatically.

## Workflow: `android-build.yml`

**Triggers:**
- Push to `main` or `develop` branch
- Pull requests to `main`
- Manual trigger (workflow_dispatch)

**What it builds:**
- ✅ Debug APK (always)
- ✅ Release APK (unsigned, for testing)

**Build time:** ~10-15 minutes

## How to Download APKs

1. Go to: https://github.com/Lamont-Labs/QuantraVision/actions
2. Click on the latest successful build (green checkmark ✓)
3. Scroll down to "Artifacts" section
4. Download:
   - `quantravision-debug-apk` - For testing (install with `adb install -r`)
   - `quantravision-release-apk` - Unsigned release build

## APK Retention

Artifacts are kept for 30 days automatically.

## Manual Trigger

To trigger a build manually:
1. Go to: https://github.com/Lamont-Labs/QuantraVision/actions
2. Click "Android CI Build" on the left
3. Click "Run workflow" button
4. Select branch and click "Run workflow"

## Why GitHub Actions instead of Codemagic?

- ✅ Free for public repositories
- ✅ No cache issues
- ✅ Fresh source checkout every build
- ✅ Transparent build logs
- ✅ Direct artifact downloads
