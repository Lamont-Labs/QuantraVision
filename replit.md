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
-   **Gradle:** 8.10.2 with AGP 8.5.0
-   **Kotlin:** 1.9.24 with legacy Compose setup
-   **AndroidX Core:** 1.13.1
-   **Jetpack Compose:** UI 1.6.8, Material3 1.2.1, Activity 1.8.2
-   **Room Database:** 2.6.1
-   **Navigation Compose:** 2.7.7
-   **TensorFlow Lite:** 2.13.0 with GPU support
-   **OpenCV:** 4.10.0 (official Maven Central AAR)
-   **CameraX:** 1.3.4
-   **Billing Library:** 6.2.1
-   **Coroutines:** 1.7.3
-   **Timber:** 5.0.1
-   **Gson:** 2.10.1
-   **SnakeYAML:** 2.0

## Recent Changes

### Session 10.2 - Kotlin Downgrade for KAPT Compatibility (October 30, 2025)
Downgraded Kotlin to restore GitHub Actions build stability and fixed file corruption issues:

**Issue 1:** Kotlin 2.0.21 incompatible with KAPT - KAPT uses K1 compiler internally which expects Kotlin 1.9.x, causing version resolution errors looking for non-existent version 1.9.25

**Issue 2:** 7 Kotlin source files had corrupted endings (```0 appended), causing compilation errors:
- OverlayButton.kt, IndicatorDetector.kt, ChartTypeRouter.kt, LegendOCR.kt, MacroRecorder.kt, AccessibilityLocaleHelper.kt, LatencyProfilerHUD.kt

**Solution: Downgrade to Kotlin 1.9.24 + Compatible Dependencies**
- Kotlin: 2.0.21 → 1.9.24 (stable, KAPT-compatible)
- AGP: 8.6.0 → 8.5.0 (compatible with Kotlin 1.9.24)
- Reverted Compose compiler plugin to old setup with `kotlinCompilerExtensionVersion = "1.5.14"`
- Removed all resolution strategy workarounds
- Downgraded AndroidX to Kotlin 1.9-compatible versions:
  - Room: 2.8.3 → 2.6.1
  - Billing: 8.0.0 → 6.2.1
  - Compose UI: 1.7.5 → 1.6.8
  - Activity Compose: 1.9.3 → 1.8.2
  - Material3: 1.3.1 → 1.2.1
  - Navigation: 2.8.5 → 2.7.7
  - Core KTX: 1.15.0 → 1.13.1
  - Coroutines: 1.10.2 → 1.7.3
  - TensorFlow Lite: 2.17.0 → 2.13.0
  - CameraX: 1.5.0 → 1.3.4
  - OpenCV: 4.10.0 (verified Maven Central availability)

**Impact:**
- Proven stable build configuration with compatible dependency versions
- All dependencies built with Kotlin 1.9.x (binary compatible)
- Still targets Android 15 (API 35)
- GitHub Actions CI/CD should now build successfully

**Future Migration Path:**
- When ready to upgrade to Kotlin 2.0+, migrate KAPT → KSP (Kotlin Symbol Processing)
- Room 2.6.1+ supports KSP fully
