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
-   **Branding:** App icon overlay button uses official Q+V geometric logo for consistent brand identity.

**Technical Implementations & Feature Specifications:**
-   **Pattern Library:** 108 deterministic chart patterns with multi-scale detection, temporal stability, and confidence calibration using 119 OpenCV template reference images.
-   **Gamification:** Includes 15 achievements, daily streaks, and user statistics.
-   **Predictive Intelligence:** Features early pattern detection (40-85% formation), formation velocity analysis, next pattern prediction, and key level identification.
-   **Professional Analytics:** Tracks pattern performance (accuracy, frequency, confidence) and identifies hot patterns.
-   **Advanced Trading Tools:** Smart Watchlist, PDF Report Generator, Backtesting Engine (CSV import, historical validation), Multi-Chart Comparison, and Pattern Similarity Search.
-   **AI Transparency:** Provides a detection audit trail with reasoning, factor breakdown, and a warning system.
-   **Hands-Free Operation:** Supports 16 natural language voice commands with voice announcements (TTS) for pattern alerts.
-   **Education System:** 25 interactive lessons with quizzes and certification.
-   **Privacy & Performance:** 100% offline, no data collection, deterministic results, fast on-device processing.
-   **Indicator Detection:** Integrates IndicatorDetector with LegendOCR for robust legend token recognition and visual cue detection, handling complex image processing and resource management.

**Unique Competitive Features (October 2025):**
-   **Voice Announcements:** Android TextToSpeech announces pattern detections hands-free ("Head and Shoulders forming - 75% complete, strong confidence"). Supports high confidence alerts, forming pattern notifications, and invalidation warnings. All offline, no internet required.
-   **Haptic Feedback:** Multi-modal vibration patterns for different pattern types (2 buzzes = bullish, 3 buzzes = bearish, long buzz = high confidence, double long = invalidated). Provides glanceable alerts without looking at screen.
-   **Pattern Strength Scoring:** Three-tier categorization system with color coding - 🔴 Weak (40-60%), 🟡 Moderate (60-80%), 🟢 Strong (80-100%). Includes formation percentage calculation and confidence grading (A+ to F).
-   **Pattern Invalidation Detection:** Real-time detection when patterns break their formation rules (e.g., neckline broken, triangle diverged). Alerts via voice + haptic + UI notification. Stores invalidation history in database for analysis.
-   **Enhanced Smart Watchlist:** Auto-scanning watchlist with proactive alerts. Identifies top opportunities, tracks bullish/bearish pattern counts, and provides pattern clustering across timeframes. WatchlistScanner runs on configurable intervals (default 5 minutes).
-   **Multi-Modal Alert System:** Centralized AlertManager singleton coordinates voice, haptic, and visual alerts. Settings persist via SharedPreferences. All alerts 100% offline and privacy-preserving.

**System Design Choices:**
-   **Modularity:** Features are organized into distinct modules (e.g., `gamification/`, `analytics/`, `prediction/`, `alerts/`, `watchlist/`).
-   **Integration:** `PatternDetector` integrates with `FeatureIntegration`, `HighlightGate` manages quotas, and `DashboardScreen` provides central navigation.
-   **Asynchronous Operations:** All file I/O operations utilize `Dispatchers.IO`.
-   **Feature Gating:** Pro features are managed via `BillingManager`/`LicenseManager`.
-   **Resource Management:** Implements careful resource handling, especially for image processing, to prevent leaks and ensure stable performance (e.g., stride-safe YUV to Bitmap conversion, proper ML Kit TextRecognizer closure, singleton AlertManager for TTS/Vibrator lifecycle).
-   **ProGuard Configuration:** Comprehensive keep rules are in place for ML Kit and OpenCV to prevent issues in release builds.
-   **Alert Architecture:** Thread-safe singleton AlertManager with double-checked locking pattern coordinates all voice, haptic, and invalidation alerts across the app. Uses application context and SharedPreferences for lifecycle safety and preference persistence.

## External Dependencies
-   **Java:** GraalVM 22.3
-   **Android SDK:** Platform 35, Build tools 35.0.0 (Android 15)
-   **Gradle:** 8.11.1 with AGP 8.7.3
-   **Kotlin:** 2.1.0 with KSP
-   **AndroidX Core:** 1.15.0
-   **Jetpack Compose:** UI 1.7.5, Material3 1.3.1, Activity 1.9.3
-   **Room Database:** 2.6.1 with KSP
-   **Navigation Compose:** 2.8.5
-   **TensorFlow Lite:** 2.17.0 with GPU support
-   **OpenCV:** 4.10.0
-   **CameraX:** 1.5.0
-   **Billing Library:** 8.0.0
-   **Coroutines:** 1.10.1
-   **Timber:** 5.0.1
-   **Gson:** 2.11.0
-   **SnakeYAML:** 2.3