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
-   **Android SDK:** Platform 34, Build tools 34.0.0
-   **Gradle:** 8.10.2
-   **Kotlin:** Latest stable
-   **Jetpack Compose:** Modern Android UI toolkit
-   **TensorFlow Lite:** For on-device machine learning
-   **OpenCV:** For computer vision and image processing (version 4.8.0)
-   **Timber:** Logging library
-   **Gson:** JSON parsing library
-   **SnakeYAML:** YAML parsing library
-   **Navigation Compose:** For in-app navigation