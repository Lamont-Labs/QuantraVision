# 🚀 Building QuantraVision Apex on GitHub Actions

This guide explains how to build your Android APK using GitHub Actions (free automated builds in the cloud).

---

## ✅ What's Already Set Up

Your repository is **100% ready** for automated Android builds with:

- ✅ **Complete CI/CD pipeline** (.github/workflows/android-complete.yml)
- ✅ **Android SDK setup** (automatically installed)
- ✅ **Gradle caching** (faster builds)
- ✅ **120+ unit tests** (runs automatically)
- ✅ **Code quality checks** (lint reports)
- ✅ **Debug APK builds** (ready to install)
- ✅ **Release APK builds** (unsigned, for testing)
- ✅ **Error extraction** (detailed failure reports)

---

## 🎯 How to Trigger a Build

### Method 1: Push to GitHub (Automatic)

Every time you push code to `main` or `develop` branch, a build starts automatically:

```bash
# Commit your changes
git add .
git commit -m "Your commit message"

# Push to trigger build
git push origin main
```

Within seconds, GitHub Actions will start building your APK.

### Method 2: Manual Trigger

To build without pushing code:

1. Go to: https://github.com/YOUR-USERNAME/QuantraVision/actions
2. Click **"Android Build & Test"** on the left
3. Click **"Run workflow"** dropdown
4. Select branch (usually `main`)
5. Click green **"Run workflow"** button

---

## 📊 Monitoring Your Build

### Step 1: Open GitHub Actions
Go to: https://github.com/YOUR-USERNAME/QuantraVision/actions

### Step 2: Find Your Build
- **Yellow dot** 🟡 = Build in progress
- **Green checkmark** ✅ = Build succeeded
- **Red X** ❌ = Build failed

### Step 3: View Build Details
Click on any build to see:
- Real-time build logs
- Test results (120+ tests)
- Lint/code quality results
- Compilation errors (if any)
- Build summary with file sizes

---

## 📥 Download Your APK

### After Build Succeeds (Green ✅)

1. Open the successful build
2. Scroll down to **Artifacts** section
3. Download **quantravision-apex-debug-apk**
4. Unzip the file
5. Install on your Android device:

```bash
# Via ADB
adb install -r app-universal-debug.apk

# Or drag/drop onto device
```

---

## 📋 Build Summary Example

Every build creates a detailed summary showing:

```
# 📦 QuantraVision Apex Build Summary

Build Date: 2025-11-24 18:30:00 UTC
Commit: abc1234

## 🔍 Code Quality
✅ Lint completed - check artifacts for details

## 🧪 Unit Tests
✅ All 120 tests passed

## 📱 Build Artifacts

### Debug APK
✅ Debug APK built successfully
app-universal-debug.apk - 45.2 MB

### Release APK (Unsigned)
✅ Release APK built successfully
app-universal-release-unsigned.apk - 28.7 MB
⚠️ Note: This APK is unsigned and cannot be installed.

## 📥 Download Artifacts
- 📦 quantravision-apex-debug-apk - Debug APK ready to install
- 📊 lint-results - Code quality report
- 🧪 test-results - Unit test results
- 📝 build-logs - Complete build logs
```

---

## 🐛 If Build Fails

### 1. Check the Build Summary
GitHub Actions creates a summary showing exactly what failed.

### 2. Download Error Logs
Download the **build-logs** artifact which contains:
- `compilation-errors-debug.txt` - All Kotlin compilation errors
- `full-build-debug.log` - Complete build output
- `error-summary.txt` - Error count summary

### 3. Common Issues

**Compilation Errors**
- Download `compilation-errors-debug.txt`
- Fix the Kotlin errors listed
- Push to trigger new build

**Test Failures**
- Download `test-results` artifact
- Check which tests failed
- Fix the failing tests
- Push to trigger new build

**Build Timeout (>45 minutes)**
- Check for infinite loops in tests
- Reduce test count if excessive
- Contact support if persistent

---

## ⏱️ Build Times

Expected build times:
- **First build:** ~15 minutes (downloads dependencies)
- **Subsequent builds:** ~10 minutes (uses cache)
- **On failures:** ~5 minutes (fails fast)

---

## 🔐 Release Signing (Google Play)

Current setup builds **unsigned release APKs** (for testing only).

To build **signed APKs for Google Play:**

### 1. Generate Release Keystore
See [RELEASE_PLAYBOOK.md](RELEASE_PLAYBOOK.md) for detailed instructions.

### 2. Add GitHub Secrets
Repository Settings → Secrets → Actions → New repository secret:

- `KEYSTORE_FILE` - Your keystore file (base64 encoded)
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias
- `KEY_PASSWORD` - Key password

### 3. Update Workflow
The workflow will automatically detect and use the signing config.

---

## 💡 Tips

**Speed Up Builds**
- Use Gradle caching (already enabled)
- Keep dependencies updated
- Minimize test count if excessive

**Save Artifacts**
- Artifacts kept for 30 days
- Download important builds immediately
- Consider external storage for long-term

**Monitor Costs**
- GitHub Actions is **FREE** for public repositories
- 2,000 minutes/month for private repositories
- ~10 minutes per build = ~200 builds/month free

---

## 📚 Related Files

- `.github/workflows/android-complete.yml` - Main build workflow
- `.github/workflows/README.md` - Detailed workflow documentation
- `RELEASE_PLAYBOOK.md` - Production release guide
- `app/build.gradle.kts` - Build configuration

---

## ✅ Quick Checklist

Before your first build:

- [ ] Code pushed to GitHub
- [ ] Repository is public (or has Actions enabled)
- [ ] No local-only dependencies
- [ ] All secrets stored in GitHub Secrets (not in code)
- [ ] Tests pass locally

---

## 🆘 Need Help?

**Build Failing?**
1. Check build summary in GitHub Actions
2. Download build-logs artifact
3. Review error messages
4. Fix and push again

**APK Won't Install?**
- Use the **debug** APK, not release (unsigned)
- Enable "Install from unknown sources" on Android
- Check APK is for correct architecture (arm64-v8a)

**Questions?**
- Check `.github/workflows/README.md`
- Review `RELEASE_PLAYBOOK.md`
- Open an issue on GitHub

---

**Ready to build?** Just push to GitHub and watch the magic happen! 🎉

**Last Updated:** November 24, 2025  
**Maintained By:** Lamont Labs
