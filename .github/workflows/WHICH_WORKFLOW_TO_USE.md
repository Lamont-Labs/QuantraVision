# ⚠️ Which Workflow Should You Use?

## ✅ USE THIS ONE: `android-complete.yml`

This is the **recommended and fully-configured** workflow for building QuantraVision Apex.

**Features:**
- ✅ Gradle configuration cache **disabled** (prevents OpenCV dependency errors)
- ✅ Complete cache cleanup before build
- ✅ Comprehensive error reporting
- ✅ Lint + Tests + APK builds
- ✅ Detailed build summaries

**Triggers:**
- Push to `main` or `develop`
- Pull requests to `main`
- Manual dispatch

---

## ❌ LEGACY WORKFLOWS (DO NOT USE)

The following workflows exist but should **NOT be used**:

### `ci.yml` - Basic CI (Superseded)
- Missing OpenCV dependency fixes
- No comprehensive error reporting
- **Recommendation:** Delete or disable this workflow

### `android-build.yml` - Alternative Build (Superseded)
- Missing configuration cache fixes
- Less comprehensive than android-complete.yml
- **Recommendation:** Delete or disable this workflow

### `android-ci.yml` - Alternative CI (Superseded)
- **This is the one that failed** with OpenCV errors
- Uses aggressive Gradle cache settings that conflict with OpenCV
- **Recommendation:** Delete or disable this workflow

---

## 🔧 How to Disable Legacy Workflows

**Option 1: Delete them (Recommended)**
```bash
git rm .github/workflows/ci.yml
git rm .github/workflows/android-build.yml
git rm .github/workflows/android-ci.yml
git commit -m "Remove legacy workflows, use android-complete.yml only"
git push
```

**Option 2: Rename them (Safer)**
```bash
# GitHub ignores workflows not ending in .yml
mv .github/workflows/ci.yml .github/workflows/ci.yml.disabled
mv .github/workflows/android-build.yml .github/workflows/android-build.yml.disabled
mv .github/workflows/android-ci.yml .github/workflows/android-ci.yml.disabled
git add .github/workflows/*.disabled
git commit -m "Disable legacy workflows"
git push
```

**Option 3: Keep them but don't trigger**
Edit each legacy workflow and remove the `on:` trigger section so they never run automatically.

---

## 🚀 Next Build

Your **next push** will automatically use `android-complete.yml` and should succeed because:

1. ✅ Gradle configuration cache is now **disabled** in `gradle.properties`
2. ✅ Workflow explicitly cleans Gradle cache and disables config cache
3. ✅ OpenCV 4.12.0 will resolve correctly without cache conflicts

---

## 📊 Expected Build Time

- **First build:** ~12-15 minutes (downloads all dependencies fresh)
- **Subsequent builds:** ~10-12 minutes (uses Gradle dependency cache, but not config cache)

---

## ✅ Verification

After your next push, check:
1. GitHub Actions → Click on the running build
2. Verify it says "**Android Build & Test**" (not "Android CI")
3. Watch the "Clean build and Gradle cache" step - should remove old cache
4. Build should complete successfully with debug APK artifact

---

**Last Updated:** November 24, 2025  
**Issue Fixed:** OpenCV dependency resolution error (configuration cache conflict)
