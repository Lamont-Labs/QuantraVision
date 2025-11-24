# GitHub Actions CI/CD for QuantraVision Apex

This repository uses GitHub Actions to automatically build Android APKs on every push.

## 🚀 Primary Workflow: `android-complete.yml`

**Recommended workflow** - Comprehensive build pipeline with full testing and reporting.

### Triggers
- ✅ Push to `main` or `develop` branch
- ✅ Pull requests to `main`
- ✅ Manual trigger (workflow_dispatch)

### What it Does
1. **Code Quality**
   - Runs Android Lint checks
   - Uploads lint reports as artifacts

2. **Unit Tests**
   - Runs all 120+ unit tests
   - Uploads test results and coverage reports

3. **Build APKs**
   - ✅ **Debug APK** - Ready to install on devices for testing
   - ✅ **Release APK (unsigned)** - Optimized but not signed for Google Play

4. **Error Reporting**
   - Extracts compilation errors from build logs
   - Creates detailed build summary with error details
   - Uploads complete build logs for debugging

### Build Time
⏱️ Approximately 10-15 minutes

### Build Summary
Every build creates a detailed summary showing:
- ✅ Code quality results
- ✅ Test pass/fail status with counts
- ✅ APK build status and file sizes
- ✅ Compilation errors (if any)
- ✅ Links to download artifacts

---

## 📥 How to Download APKs

### From GitHub Actions
1. Go to: https://github.com/Lamont-Labs/QuantraVision/actions
2. Click on the latest successful build (green checkmark ✓)
3. Scroll down to the **Artifacts** section
4. Download:
   - `quantravision-apex-debug-apk` - **Install this on your device**
   - `lint-results` - Code quality report
   - `test-results` - Unit test results
   - `build-logs` - Complete build logs (for debugging failures)

### Install Debug APK on Device
```bash
# Via ADB
adb install -r quantravision-apex-debug.apk

# Or drag/drop the APK file onto your device
```

---

## 🎯 Manual Build Trigger

To trigger a build manually:

1. Go to: https://github.com/Lamont-Labs/QuantraVision/actions
2. Click **"Android Build & Test"** on the left sidebar
3. Click the **"Run workflow"** dropdown button
4. Select your branch (usually `main`)
5. Click **"Run workflow"** green button

The build will start immediately.

---

## 📊 Artifact Retention

All artifacts are kept for **30 days** automatically, then deleted.

---

## 🔐 Signing Configuration

### Debug Builds
- **Automatically signed** with debug keystore
- **Can be installed immediately** on any device
- **For testing only** - not for Google Play

### Release Builds
- **Currently unsigned** - will build successfully but cannot be installed
- **To enable signing for Google Play:**
  1. Generate a release keystore (see [RELEASE_PLAYBOOK.md](../../RELEASE_PLAYBOOK.md))
  2. Add secrets to GitHub repository:
     - `KEYSTORE_FILE` (base64 encoded keystore)
     - `KEYSTORE_PASSWORD`
     - `KEY_ALIAS`
     - `KEY_PASSWORD`
  3. Update workflow to decode and use keystore

---

## 🛠️ Other Workflows (Legacy)

The following workflows exist but are **not recommended** for primary use:

- ❌ `ci.yml` - Basic CI (superseded by android-complete.yml)
- ❌ `android-build.yml` - Alternative build config (superseded by android-complete.yml)
- ❌ `android-ci.yml` - Alternative CI config (superseded by android-complete.yml)

**Recommendation:** Use `android-complete.yml` for all builds. Consider disabling or removing the legacy workflows.

---

## 🏗️ Build Environment

The builds run on GitHub-hosted runners with:
- **OS:** Ubuntu Latest
- **JDK:** 17 (Temurin)
- **Android SDK:** Latest (installed via android-actions/setup-android)
- **Gradle:** Wrapper version specified in project
- **Memory:** 6GB JVM heap, 4GB Kotlin daemon
- **Timeout:** 45 minutes maximum

---

## ✅ Why GitHub Actions?

✅ **Free** for public repositories  
✅ **Reliable** - fresh environment every build  
✅ **Transparent** - detailed logs and error reporting  
✅ **Integrated** - automatic builds on every push  
✅ **Artifact hosting** - 30-day retention of APKs  
✅ **No setup required** - works out of the box  

---

## 🐛 Troubleshooting

### Build Fails with Compilation Errors
1. Download the `build-logs` artifact
2. Check `compilation-errors-debug.txt` for error details
3. Fix the errors in your code
4. Push to trigger a new build

### Build Times Out
- Default timeout is 45 minutes
- If builds consistently timeout, check for:
  - Excessive test count
  - Large asset files
  - Network-dependent tests

### APK Not Generated
- Check the build summary in the workflow run
- Download `build-logs` artifact for full error details
- Verify no compilation errors in the logs

---

## 📚 Related Documentation

- [RELEASE_PLAYBOOK.md](../../RELEASE_PLAYBOOK.md) - Production release guide with signing instructions
- [ARCHITECTURE.md](../../docs/ARCHITECTURE.md) - System architecture and design
- [CONTRIBUTING.md](../../CONTRIBUTING.md) - Contribution guidelines

---

**Last Updated:** November 24, 2025  
**Maintained By:** Lamont Labs
