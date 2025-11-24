# Release Playbook

**QuantraVision Android Application Release Guide**

This document provides comprehensive instructions for building, signing, and releasing QuantraVision to production.

---

## 📋 Table of Contents

- [Version Numbering](#-version-numbering)
- [Build Types](#-build-types)
- [Pre-Release Checklist](#-pre-release-checklist)
- [Build Instructions](#️-build-instructions)
- [Signing Configuration](#-signing-configuration)
- [ProGuard Configuration](#-proguard-configuration)
- [APK/AAB Generation](#-apkaab-generation)
- [Google Play Console Upload](#-google-play-console-upload)
- [Rollback Procedures](#-rollback-procedures)
- [Post-Release Monitoring](#-post-release-monitoring)

---

## 📦 Version Numbering

QuantraVision follows **Semantic Versioning 2.0.0**:

```
MAJOR.MINOR.PATCH
```

### Version Components

- **MAJOR**: Incompatible API changes or major feature releases
- **MINOR**: Backward-compatible new features or significant enhancements
- **PATCH**: Backward-compatible bug fixes or minor improvements

### Examples

- `1.0.0` - Initial production release
- `1.1.0` - Added geometric pattern detection (new feature)
- `1.1.1` - Fixed memory leak in pattern cache (bug fix)
- `2.0.0` - Complete UI redesign (breaking change)

### Version Code

Android `versionCode` must increment sequentially for each release:

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = 10   // Increment by 1 for each release
        versionName = "1.2.3"
    }
}
```

**Formula**: `versionCode = (MAJOR * 10000) + (MINOR * 100) + PATCH`

- Version 1.0.0 → versionCode 10000
- Version 1.2.3 → versionCode 10203
- Version 2.0.0 → versionCode 20000

---

## 🔨 Build Types

### Debug Build

**Purpose**: Development and testing

**Configuration**:
```kotlin
buildTypes {
    getByName("debug") {
        applicationIdSuffix = ".debug"
        isDebuggable = true
        isMinifyEnabled = false
        isShrinkResources = false
    }
}
```

**Features**:
- Debuggable via Android Studio
- No code obfuscation
- Larger APK size
- Debug signing certificate (auto-generated)

### Release Build

**Purpose**: Production distribution via Google Play

**Configuration**:
```kotlin
buildTypes {
    getByName("release") {
        isDebuggable = false
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

**Features**:
- Code obfuscation via ProGuard/R8
- Resource shrinking (smaller APK)
- Release signing certificate (production key)
- Not debuggable

---

## ✅ Pre-Release Checklist

Complete all items before building a release:

### Code Quality

- [ ] All unit tests passing (`./gradlew test`)
- [ ] All instrumentation tests passing (`./gradlew connectedAndroidTest`)
- [ ] Lint checks passing with no critical warnings (`./gradlew lint`)
- [ ] Code coverage ≥80% for business logic
- [ ] No TODO/FIXME comments in production code
- [ ] No debug logging or print statements
- [ ] ProGuard rules tested and validated

### Documentation

- [ ] CHANGELOG.md updated with release notes
- [ ] Version numbers updated in `build.gradle.kts`
- [ ] README.md reflects latest features
- [ ] All public APIs have KDoc comments
- [ ] SECURITY.md updated if applicable

### Security

- [ ] No hardcoded API keys or secrets
- [ ] All secrets stored in Android Keystore or secure config
- [ ] Permissions justified and documented
- [ ] Security audit completed (if major release)
- [ ] Third-party dependencies scanned for vulnerabilities

### Functional Testing

- [ ] Manual testing on physical device (Samsung S23 FE or similar)
- [ ] Pattern detection accuracy validated (≥60% on test dataset)
- [ ] Billing flow tested (all tiers: FREE, STARTER, PRO)
- [ ] Cloud narration tested (quota limits, fallback behavior)
- [ ] Offline mode tested (no network required for FREE tier)
- [ ] Performance benchmarks met (2-4 FPS scan rate)

### Legal & Compliance

- [ ] Privacy policy updated (if data collection changes)
- [ ] Terms of use reviewed
- [ ] Apache-2.0 license headers on all source files
- [ ] Third-party licenses documented in app

---

## 🛠️ Build Instructions

### Prerequisites

**Required**:
- Android Studio Ladybug (2024.2.1+)
- JDK 17
- Gradle 8.7+ (included via wrapper)
- Release keystore file (`quantravision-release.jks`)
- Keystore password and key alias

### Debug Build

```bash
# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build

```bash
# Clean previous builds
./gradlew clean

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Android App Bundle (AAB)

**Recommended for Google Play uploads** (smaller download size):

```bash
# Build release AAB
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

---

## 🔐 Signing Configuration

### Generate Release Keystore

**First-time setup only**:

```bash
keytool -genkey -v \
  -keystore quantravision-release.jks \
  -keyalg RSA \
  -keysize 4096 \
  -validity 25000 \
  -alias quantravision-key
```

**Enter information**:
- Keystore password: **[SECURE PASSWORD - STORE IN PASSWORD MANAGER]**
- Key password: **[SECURE PASSWORD - SAME OR DIFFERENT]**
- Name: Lamont Labs
- Organization: Lamont Labs
- City/State/Country: Your location

**⚠️ CRITICAL**: Backup keystore file to secure location (password manager, encrypted backup). If lost, you cannot update the app on Google Play.

### Configure Signing in build.gradle.kts

**Option 1: Environment Variables (Recommended for CI/CD)**

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("QUANTRA_KEYSTORE_PATH") ?: "quantravision-release.jks")
            storePassword = System.getenv("QUANTRA_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("QUANTRA_KEY_ALIAS")
            keyPassword = System.getenv("QUANTRA_KEY_PASSWORD")
        }
    }
    
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

**Set environment variables**:
```bash
export QUANTRA_KEYSTORE_PATH=/path/to/quantravision-release.jks
export QUANTRA_KEYSTORE_PASSWORD=your_keystore_password
export QUANTRA_KEY_ALIAS=quantravision-key
export QUANTRA_KEY_PASSWORD=your_key_password
```

**Option 2: keystore.properties File (Local Development)**

Create `keystore.properties` (DO NOT commit to Git):

```properties
storeFile=/path/to/quantravision-release.jks
storePassword=your_keystore_password
keyAlias=quantravision-key
keyPassword=your_key_password
```

**Add to .gitignore**:
```gitignore
keystore.properties
*.jks
*.keystore
```

**Load in build.gradle.kts**:
```kotlin
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] ?: "")
            storePassword = keystoreProperties["storePassword"] as String?
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
        }
    }
}
```

---

## 🛡️ ProGuard Configuration

ProGuard/R8 obfuscates code and removes unused code to reduce APK size.

### Location

`app/proguard-rules.pro`

### Standard Configuration

```proguard
# ===================================
# QuantraVision ProGuard Rules
# Version: 1.0
# Last Updated: November 24, 2025
# ===================================

# ===================================
# STANDARD ANDROID RULES
# ===================================

# Keep Android framework classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ===================================
# QUANTRAVISION-SPECIFIC RULES
# ===================================

# Keep all Apex Protocol classes (used via reflection)
-keep class com.lamontlabs.quantravision.apex.** { *; }
-keepclassmembers class com.lamontlabs.quantravision.apex.** {
    <fields>;
    <methods>;
}

# Keep billing classes (Google Play Billing API)
-keep class com.lamontlabs.quantravision.billing.** { *; }
-keep class com.android.billingclient.** { *; }

# Keep Room database entities and DAOs
-keep class com.lamontlabs.quantravision.data.** { *; }
-keepclassmembers class com.lamontlabs.quantravision.data.** {
    <fields>;
}

# Keep ML models (TensorFlow Lite, OpenCV)
-keep class org.tensorflow.lite.** { *; }
-keep class org.opencv.** { *; }

# Keep Cloud Narration API models (JSON serialization)
-keep class com.lamontlabs.quantravision.cloud.** { *; }
-keepclassmembers class com.lamontlabs.quantravision.cloud.** {
    <fields>;
}

# ===================================
# OPTIMIZATION SETTINGS
# ===================================

# Enable aggressive optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization options
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ===================================
# OBFUSCATION SETTINGS
# ===================================

# Keep source file names and line numbers for crash reports
-keepattributes SourceFile,LineNumberTable

# Keep generic signatures for reflection
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*

# ===================================
# WARNINGS SUPPRESSION
# ===================================

# Suppress warnings for missing classes (third-party libraries)
-dontwarn org.tensorflow.**
-dontwarn org.opencv.**
-dontwarn okhttp3.**
-dontwarn okio.**
```

### Testing ProGuard Rules

**1. Build release APK**:
```bash
./gradlew assembleRelease
```

**2. Install and test on device**:
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

**3. Test all features**:
- Pattern detection
- Billing flow
- Cloud narration
- Settings
- Database operations

**4. Check for ProGuard-related crashes**:
```bash
adb logcat | grep -E "(ProGuard|R8|StackTrace)"
```

**5. Add rules if needed**:
If a feature breaks, add specific `-keep` rules for affected classes.

---

## 📦 APK/AAB Generation

### APK (Android Package)

**Use case**: Direct installation, testing, internal distribution

```bash
./gradlew assembleRelease
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

**Size**: ~30-50 MB (includes all architectures and densities)

### AAB (Android App Bundle)

**Use case**: Google Play Store uploads (recommended)

```bash
./gradlew bundleRelease
```

**Output**: `app/build/outputs/bundle/release/app-release.aab`

**Advantages**:
- Smaller downloads (Google Play generates optimized APKs per device)
- Supports Dynamic Feature Modules
- Required for apps >150 MB

**Size**: ~25-35 MB (before Play Store optimization)

### Universal APK from AAB

Generate a universal APK from AAB for testing:

```bash
bundletool build-apks \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app-release.apks \
  --mode=universal

unzip app-release.apks -d apks/
# Install: adb install apks/universal.apk
```

---

## 🚀 Google Play Console Upload

### Prerequisites

- Google Play Developer account ($25 one-time fee)
- App listing created in Play Console
- Release build (AAB) ready
- All store assets prepared (screenshots, descriptions, icon)

### Upload Steps

**1. Login to Google Play Console**:
https://play.google.com/console/

**2. Select QuantraVision app**:
Navigate to your app from the app list

**3. Create new release**:
- Production → Create new release
- Or: Internal testing / Closed testing / Open testing

**4. Upload AAB**:
- Drag and drop `app-release.aab`
- Or click "Upload" and select file

**5. Release notes**:
```markdown
Version 1.2.0 - November 24, 2025

New Features:
- Added geometric pattern detection for improved accuracy
- Cloud narration with GPT-4 integration (PRO tier)

Improvements:
- Optimized pattern detection speed (2x faster)
- Reduced memory usage by 30%

Bug Fixes:
- Fixed crash on Android 14 devices
- Resolved billing synchronization issue
```

**6. Review and rollout**:
- Review app content
- Set rollout percentage (100% for full release, or phased rollout: 10% → 50% → 100%)
- Click "Start rollout to production"

### Rollout Timeline

- **Review**: 1-3 days (Google's automated + manual review)
- **Staged rollout**: Control exposure (10% → 50% → 100% over days)
- **Full availability**: 24-48 hours after approval

---

## 🔄 Rollback Procedures

### Emergency Rollback

If critical bug discovered post-release:

**Option 1: Halt Rollout (Before 100%)**

1. Play Console → Production → Active release
2. Click "Halt rollout"
3. Fix bug locally
4. Build new release with patch
5. Upload new version

**Option 2: Rollback to Previous Version**

1. Play Console → Production → Releases
2. Select previous stable version
3. Click "Roll back to this version"
4. Confirm rollback

**⚠️ Limitations**:
- Cannot roll back to version with lower `versionCode`
- All users will need to update to previous version
- May take 24-48 hours to propagate

### Hotfix Process

For critical bugs:

1. **Create hotfix branch**:
   ```bash
   git checkout -b hotfix/1.2.1 v1.2.0
   ```

2. **Fix bug and test thoroughly**:
   ```bash
   # Make changes
   git commit -m "fix: resolve crash on Android 14"
   ```

3. **Increment patch version**:
   ```kotlin
   versionCode = 10201  // Was 10200
   versionName = "1.2.1"
   ```

4. **Build and upload immediately**:
   ```bash
   ./gradlew bundleRelease
   # Upload to Play Console
   ```

5. **Expedited review**:
   - Contact Google Play Support if urgent
   - Explain critical nature of fix
   - Request expedited review

6. **Merge to main**:
   ```bash
   git checkout main
   git merge hotfix/1.2.1
   git tag v1.2.1
   git push origin main --tags
   ```

---

## 📊 Post-Release Monitoring

### Metrics to Monitor

**Play Console Dashboards**:

1. **Crashes & ANRs**:
   - Android Vitals → Crashes
   - Target: <0.5% crash rate
   - Action: Investigate top crashes immediately

2. **Ratings & Reviews**:
   - User reviews (filter by star rating)
   - Action: Respond to 1-2 star reviews within 48 hours

3. **Installation Analytics**:
   - Install/uninstall rate
   - Device compatibility issues
   - Target: <10% uninstall rate

4. **Performance Metrics**:
   - App startup time (target: <2 seconds)
   - Battery usage (target: <5% per hour active use)
   - Network usage (target: <1 MB/day for FREE tier)

### Internal Monitoring

**GitHub Actions CI/CD**:
- Monitor build status
- Review test failures

**User Support**:
- Monitor support emails (support@lamontlabs.com)
- Track common issues in issue tracker
- Create FAQ based on recurring questions

---

## 🔗 Additional Resources

- **Google Play Console**: https://play.google.com/console/
- **Android Publishing Guide**: https://developer.android.com/studio/publish
- **ProGuard Manual**: https://www.guardsquare.com/manual/home
- **Bundletool Documentation**: https://developer.android.com/studio/command-line/bundletool

---

**Last Updated**: November 24, 2025  
**Maintainer**: Lamont Labs  
**Contact**: support@lamontlabs.com
