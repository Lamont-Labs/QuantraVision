# QuantraVision - Advanced AI Pattern Detection

## Overview
QuantraVision is an offline AI pattern detection application designed for retail traders. It offers 108 chart patterns, gamification, predictive analytics, professional reporting, and educational content. The project's core purpose is to provide advanced trading tools with a focus on user privacy and on-device processing, eliminating the need for subscriptions or internet connectivity. It aims to deliver a blend of learning, engagement, and powerful analytical capabilities for traders of all experience levels.

## User Preferences
I prefer that you work iteratively, proposing changes and asking for my approval before implementing them. Please provide detailed explanations for any significant modifications you suggest or make. I value clear, concise communication and prefer that you focus on high-level solutions before diving into implementation specifics. Do not make changes to the existing project structure without explicit approval.

## System Architecture
QuantraVision is developed using Kotlin and Jetpack Compose, following modern Android development best practices. It is designed for 100% offline operation to ensure user data privacy.

**UI/UX Decisions:**
-   **Design System:** Material 3 Design with Lamont Labs branding.
-   **Theme:** Dark theme optimized (#0A1218 background, #00E5FF cyan accent).
-   **Responsiveness:** Responsive navigation and layout for various Android devices.
-   **Widgets:** Home screen widget for quick access.

**Technical Implementations & Feature Specifications:**
-   **Pattern Library:** 108 deterministic chart patterns with multi-scale detection, temporal stability, and confidence calibration using 119 OpenCV template reference images.
-   **Gamification:** Includes 15 achievements, daily streaks, and user statistics.
-   **Predictive Intelligence:** Features early pattern detection (40-85% formation), formation velocity analysis, next pattern prediction, and key level identification.
-   **Professional Analytics:** Tracks pattern performance (accuracy, frequency, confidence) and identifies hot patterns.
-   **Advanced Trading Tools:** Smart Watchlist, PDF Report Generator, Backtesting Engine (CSV import, historical validation), Multi-Chart Comparison, and Pattern Similarity Search.
-   **AI Transparency:** Provides a detection audit trail with reasoning, factor breakdown, and a warning system.
-   **Hands-Free Operation:** Supports 16 natural language voice commands.
-   **Education System:** 25 interactive lessons with quizzes and certification.
-   **Privacy & Performance:** 100% offline, no data collection, deterministic results, fast on-device processing.

**System Design Choices:**
-   **Modularity:** Features are organized into distinct modules (e.g., `gamification/`, `analytics/`, `prediction/`).
-   **Integration:** `PatternDetector` integrates with `FeatureIntegration`, `HighlightGate` manages quotas, and `DashboardScreen` provides central navigation.
-   **Asynchronous Operations:** All file I/O operations utilize `Dispatchers.IO`.
-   **Feature Gating:** Pro features are managed via `BillingManager`/`LicenseManager`.

## External Dependencies
-   **Java:** GraalVM 22.3
-   **Android SDK:** Platform 35, Build tools 35.0.0 (Android 15)
-   **Gradle:** 8.10.2 with AGP 8.6.0
-   **Kotlin:** 2.0.21 with Compose compiler plugin
-   **AndroidX Core:** 1.15.0 (API 35 compatible)
-   **Jetpack Compose:** UI 1.7.5, Material3 1.3.1, Activity 1.9.3
-   **Room Database:** 2.8.3
-   **Navigation Compose:** 2.8.5
-   **TensorFlow Lite:** 2.17.0 with GPU support
-   **OpenCV:** 4.10.0 (official Maven Central AAR)
-   **CameraX:** 1.5.0
-   **Billing Library:** 8.0.0
-   **Coroutines:** 1.10.2
-   **Timber:** 5.0.1
-   **Gson:** 2.11.0
-   **SnakeYAML:** 2.3

## Recent Changes

### Session 10.1 - TensorFlow Lite Duplicate Class Fix (October 30, 2025)
Fixed duplicate class conflict in TensorFlow Lite 2.17.0:

**Issue:** TensorFlow Lite 2.17.0 uses new LiteRT packages internally, causing duplicate class errors:
- `org.tensorflow.lite.DataType` found in both litert-api-1.0.1.aar and tensorflow-lite-api-2.13.0.aar
- Root cause: `tensorflow-lite-support:0.4.4` transitively pulls in old `tensorflow-lite-api:2.13.0`

**Fix:** Excluded old tensorflow-lite-api from tensorflow-lite-support dependency
```kotlin
implementation("org.tensorflow:tensorflow-lite-support:0.4.4") {
    exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
}
```

**Impact:** Resolves AAR duplicate class errors, allows TensorFlow Lite 2.17.0 to use new LiteRT packages without conflicts

### Session 10 - Comprehensive Dependency Upgrade & Workflow Cleanup (October 30, 2025)
Upgraded all dependencies to latest stable versions (October 2025) and cleaned up CI/CD workflows:

**Kotlin Toolchain:**
- Kotlin: 1.9.24 → 2.0.21 (K2 compiler)
- AGP (Android Gradle Plugin): 8.5.0 → 8.6.0
- Compose Compiler Plugin: 2.0.21 (applied directly in app module, replaces kotlinCompilerExtensionVersion)

**Android SDK:**
- compileSdk: 34 → 35 (Android 15)
- targetSdk: 34 → 35 (Required for Google Play by Aug 31, 2025)
- versionCode: 20 → 21

**Major Library Upgrades (API 35 Compatible):**
- AndroidX Core KTX: 1.13.1 → 1.15.0
- AndroidX Activity Compose: 1.8.0 → 1.9.3
- Compose UI: 1.7.2 → 1.7.5
- Material3: 1.3.0 → 1.3.1
- Room Database: 2.6.1 → 2.8.3
- Navigation: 2.7.7 → 2.8.5
- Billing: 6.2.1 → 8.0.0 (major update)
- TensorFlow Lite: 2.13.0 → 2.17.0
- CameraX: 1.3.4 → 1.5.0
- Coroutines: 1.7.3 → 1.10.2
- Gson: 2.10.1 → 2.11.0
- SnakeYAML: 2.0 → 2.3
- OpenCV: 4.8.0 → 4.10.0

**Note:** Avoided bleeding-edge versions (1.11.0, 1.17.0, 1.9.4, 2.9.5) that require unreleased API 36

**Configuration Changes:**
- JVM Target: Java 1.8 → Java 17 (required for AGP 8.6.0+)

**CI/CD Workflow Cleanup:**
- Consolidated duplicate Android workflows into single android-ci.yml
- Added Android SDK setup step for GitHub Actions builds
- Removed deploy-pages.yml (not applicable for Android app)
- Removed redundant android.yml (duplicate of android-build.yml)
- Updated workflow with JDK 17 and Android SDK configuration

**Benefits:**
- Latest security patches and bug fixes
- Improved performance and stability
- Android 15 compliance for Google Play
- New Compose features (autofill, haptics)
- Billing Library 8.0 with automatic reconnection

### Session 9 - Deep Security Scan (October 29, 2025)
Fixed 3 critical vulnerabilities through comprehensive 10× deep debug scan:

**Bug 41 - ANR/Battery Drain (OverlayService.kt):**
- Fixed: `while(true)` → `while(isActive)` in coroutine detection loop
- Impact: Prevents infinite loop causing ANR and battery drain when service is stopped

**Bug 42 - Integer Overflow (LegendOCR.kt):**
- Fixed: Added overflow check before ByteArray(ySize + uSize + vSize) allocation
- Impact: Prevents crashes from integer overflow in image buffer calculations

**Bug 43 - Path Traversal Vulnerability (TemplateImporter.kt):**
- Fixed: Added canonical path validation with File.separator check
- Impact: CRITICAL - Prevents malicious ZIP files from writing outside app directory
- Security: Blocks `../../../etc/passwd` and absolute path prefix-sharing attacks

**Critical Patterns Documented:**
1. Coroutine loops MUST check `isActive` to respect cancellation
2. ByteArray allocations from external sources require Long arithmetic + bounds checking
3. File path operations MUST use canonical path validation: `destCanonical.startsWith(baseCanonical + File.separator)`
4. BillingManager requires manual cleanup() call to avoid scope leak

**Total Bugs Fixed:** 43 critical issues across all sessions (40 previous + 3 new)