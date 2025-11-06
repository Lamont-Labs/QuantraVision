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

## Touch Pass-Through Research (For Future Implementation)

### Android 12+ Compatibility Issue
**Problem:** Android 12+ blocks touches through overlays that are >80% opaque.
**Solution:** Add `params.alpha = 0.8f` to overlay window params in:
- `OverlayService.kt` lines 77-87 (main overlay)
- `OverlayService.kt` lines 112-122 (glowing border)

### Floating Menu Touch Blocking
**Problem:** Menu blocks all touches when visible (MATCH_PARENT size without FLAG_NOT_TOUCH_MODAL).
**Solution:** Add `FLAG_NOT_TOUCH_MODAL` to FloatingMenu.kt params (line 28-30).

### Key Findings
- Current implementation correctly uses `FLAG_NOT_TOUCHABLE` for pass-through overlays
- FloatingLogoButton properly configured with `FLAG_NOT_TOUCH_MODAL` + `WRAP_CONTENT`
- Main issue: Android 14 (Samsung S23 FE) enforces stricter opacity rules
- **Deferred:** Wait to implement due to previous issues with touch handling

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

### November 6, 2025 - QuantraCore Theme Implementation
**Complete theme overhaul matching QuantraCore reference branding:**

**Color Palette Updates:**
- Background: #0A1420 (deep navy, QuantraCore exact match)
- Primary cyan: #00E5FF (neon holographic glow)
- Cyan bright: #00F0FF (mid-layer emphasis)
- Cyan brightest: #00FFFF (sharp inner lines)
- Orange accent: #FF9800 (warm highlight, replaces old yellow)
- Gold metallic: #FFA726 (bronze/gold accents)
- Surface: #1A2332 (metallic blue-tinted dark)
- Info/Secondary: #4DD0E1 (lighter cyan for secondary text)

**Architecture Improvements:**
- Created `QuantraColors` object in Theme.kt for View-based components (bridges View/Compose layers)
- All overlay View components (LogoBadge, GlowingBorderView, LatencyProfilerHUD, ViabilityExplainer) now use QuantraColors
- Eliminated all hardcoded color literals for complete palette consistency

**Glow Effects Enhancement:**
- GlowingBorderView: Multi-layer holographic border (outer bloom 24px + mid glow 12px + inner sharp line)
- EnhancedOverlayView: Enhanced pattern highlight glows (18px blur, 75% intensity, 12px stroke width)
- Pulsing animation: High-intensity mode (63-86% opacity) vs normal ambient glow (39-70% opacity)

**UI Refinements:**
- OnboardingScreen: Scrollable layout with top alignment prevents text cutoff
- Pure white text (#FFFFFF) for maximum contrast on deep navy background
- Sharp cyan borders (60-70% opacity) with holographic bloom
- All screens use MaterialTheme.colorScheme for automatic theme propagation

**Developer Experience:**
- Single source of truth for all colors (Theme.kt)
- QuantraColors object provides integer colors for Android Views
- MaterialTheme provides Color objects for Jetpack Compose
- Future theme changes update across entire app instantly