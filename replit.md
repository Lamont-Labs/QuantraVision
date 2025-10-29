# QuantraVision - Advanced AI Pattern Detection

## Overview
QuantraVision is a comprehensive offline AI pattern detection app for retail traders. It integrates 108 chart patterns, gamification, predictive analytics, professional reporting, and educational content. Its core purpose is to provide advanced trading tools without requiring subscriptions or internet connectivity, prioritizing user privacy and on-device processing. The project aims to offer a unique blend of learning, engagement, and powerful analytical capabilities for both novice and experienced traders.

## User Preferences
I prefer that you work iteratively, proposing changes and asking for my approval before implementing them. Please provide detailed explanations for any significant modifications you suggest or make. I value clear, concise communication and prefer that you focus on high-level solutions before diving into implementation specifics. Do not make changes to the existing project structure without explicit approval.

## System Architecture
QuantraVision is built using modern Android development best practices, leveraging Kotlin, Jetpack Compose for the UI, and Gradle for builds. It is designed for 100% offline operation, ensuring user data privacy.

**UI/UX Decisions:**
-   **Design System:** Material 3 Design with Lamont Labs branding.
-   **Theme:** Dark theme optimized (#0A1218 background, #00E5FF cyan accent).
-   **Responsiveness:** Responsive navigation and layout suitable for various Android devices.
-   **Widgets:** Home screen widget for quick access to statistics.

**Technical Implementations & Feature Specifications:**
-   **Pattern Library:** 108 deterministic chart patterns with multi-scale consensus detection, temporal stability tracking, and confidence calibration. Utilizes 119 template reference images for OpenCV pattern matching.
-   **Gamification:** Features 15 achievements, daily streak tracking, and a user statistics dashboard.
-   **Predictive Intelligence:** Includes early pattern detection (40-85% formation), pattern formation velocity analysis, next pattern prediction, and key level identification.
-   **Professional Analytics:** Tracks pattern performance (accuracy, frequency, confidence), identifies hot patterns, and analyzes confidence trends.
-   **Advanced Trading Tools:**
    -   Smart Watchlist: Confluence alerts and pattern clusters.
    -   PDF Report Generator: Professional branded exports.
    -   Backtesting Engine: CSV import, historical validation, profitability analysis.
    -   Multi-Chart Comparison: Cross-asset correlation and divergence detection.
    -   Pattern Similarity Search: Finds related patterns and pattern families.
-   **AI Transparency:** Provides a detection audit trail with reasoning, factor breakdown, and a warning system for low-confidence detections.
-   **Hands-Free Operation:** Supports 16 natural language voice commands for navigation, filtering, and export controls.
-   **Education System:** Features 25 interactive lessons with quizzes and a certificate of completion.
-   **Privacy & Performance:** 100% offline operation, no data collection, deterministic results, and fast on-device processing.

**System Design Choices:**
-   **Modularity:** Features are organized into distinct modules (e.g., `gamification/`, `analytics/`, `prediction/`, `export/`, `backtesting/`, `audit/`, `voice/`, `education/`, `widget/`).
-   **Integration:** Core `PatternDetector` integrates with `FeatureIntegration`, `HighlightGate` manages daily quotas, and `DashboardScreen` provides central navigation.
-   **Asynchronous Operations:** All file I/O operations are handled asynchronously using `Dispatchers.IO` for optimal performance.
-   **Feature Gating:** Pro features are gated via a `BillingManager`/`LicenseManager`.

## External Dependencies
-   **Java:** GraalVM 22.3
-   **Android SDK:** Platform 34, Build tools 34.0.0
-   **Gradle:** 8.10.2
-   **Kotlin:** Latest stable
-   **Jetpack Compose:** Modern Android UI toolkit
-   **TensorFlow Lite:** For on-device machine learning capabilities
-   **OpenCV:** For computer vision and image processing (version 4.8.0)
-   **Timber:** Logging library
-   **Gson:** JSON parsing library
-   **SnakeYAML:** YAML parsing library
-   **Navigation Compose:** For managing in-app navigation