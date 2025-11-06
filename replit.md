# QuantraVision

## Overview
QuantraVision is an offline-first Android application designed for retail traders, offering AI-powered, on-device chart pattern recognition. It identifies 109 technical analysis patterns using advanced OpenCV template matching, providing real-time detection, predictive analysis, multi-modal alerts, and explainable AI with audit trails. The app emphasizes user privacy through on-device processing and operates without subscriptions or cloud dependencies, utilizing a 4-tier one-time payment model for lifetime access. Key features include an "Intelligence Stack" (Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, Proof Capsules) focused on offline functionality and educational support.

## User Preferences
Preferred communication style: Simple, everyday language.
Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

### UI/UX Decisions
The application utilizes Jetpack Compose with the Material 3 Design System, optimized for a dark theme, featuring a declarative and reactive UI. It employs ViewModel for state management, a modular screen architecture, responsive layouts, and a consistent brand identity. The UI prioritizes a focused user experience by streamlining the dashboard to essential features like detection, the Intelligence Hub, predictions, tutorials, a trading book, achievements, and settings. A key UX improvement includes an instant display of trade scenarios (entry, stop, target) directly on the overlay upon pattern detection for Pro tier users, and an enhanced onboarding tutorial for overlay usage.

### Technical Implementations
**Pattern Detection Engine**: Employs an OpenCV template matching system for 109 chart patterns, coordinated by a HybridDetectorBridge and utilizing a BayesianFusionEngine for confidence scoring, with TensorFlow Lite infrastructure for future ML enhancements.
**Scan Learning Engine**: (PRO tier only) Learns from chart scans to track pattern frequency, co-occurrence, and confidence distributions using perceptual image hashing for privacy-preserving, offline learning.
**Data Storage**: Utilizes an encrypted Room database for local storage of logs, user preferences, achievements, and scan learning data.
**State Management**: Implements Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Manages a four-tier lifetime access model via Google Play In-App Billing, secured with Google Play Integrity API, signature verification, and R8/ProGuard obfuscation. Critical paywall access is controlled across `BillingManager.kt`, `ProFeatureGate.kt`, `StandardFeatureGate.kt`, and `StarterFeatureGate.kt`.
**Alert System**: A centralized AlertManager provides voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay with a minimal, pulsing cyan border upon pattern detection, offering touch-passthrough to the underlying trading app.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, logging detections with SHA-256 hashes, signing pattern catalogs with Ed25519, and maintaining an SBOM.

### Feature Specifications
- **Intelligence Stack**: Comprises Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules for advanced analysis.
- **Pattern-to-Plan Overlay**: Integrates trade scenario display (entry, stop, target) directly into the overlay for instant user access (Pro tier).
- **Offline Functionality**: All core features, including AI pattern recognition and Intelligence Stack, operate entirely on-device without cloud dependencies.

## External Dependencies

### Core ML/CV Libraries
- **OpenCV**: Computer vision for template matching and image processing.
- **TensorFlow Lite**: Infrastructure for future ML enhancements.

### Android Framework
- **Kotlin**: Primary programming language.
- **Jetpack Compose**: UI framework.
- **Room**: Local database persistence.
- **Android Architecture Components**: ViewModel, LiveData, WorkManager.
- **Material 3**: Design system.
- **MediaProjection**: Screen capture APIs.

### Utilities
- **Gson**: JSON parsing.

### Billing & Security
- **Google Play Billing**: In-app purchase handling for lifetime access tiers.
- **Google Play Integrity API**: Anti-tamper verification and device integrity checks.

### Offline Assets
- **Pattern Templates**: 109 PNG reference images and YAML configurations for chart patterns.
- **Legal Documents**: HTML/Markdown for terms and privacy policy.
- **Educational Content**: Interactive lessons and the "Trading Book".

## Recent Changes

### November 6, 2025 - Pattern-to-Plan Overlay Integration & Branding
**Changes:**
- Integrated Pattern-to-Plan Engine into overlay for instant trade scenario display
- Entry/stop/target prices now show directly on pattern detections (Pro tier only)
- Simplified dashboard from 15 → 8 features (kept Achievements + Predictions)
- Fixed Room database compilation error by moving tradeScenario out of PatternMatch constructor
- All paywalls bypassed for testing (BYPASS_PAYWALLS = true in 5 files: BillingManager.kt, ProFeatureGate.kt, StandardFeatureGate.kt, StarterFeatureGate.kt, BookFeatureGate.kt)
- Updated app icon to neon "Q" logo with cyan glow (matches brand identity)
- Fixed adaptive icon cropping issue - border aligned with icon edge
- Sharpened UI theme: balanced dark background (#0A1218), pure white text, sharper cyan borders (60% opacity), neon accent colors matching icon aesthetic