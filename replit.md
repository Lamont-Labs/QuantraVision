# QuantraVision

## Overview
QuantraVision is an offline-first Android application designed for retail traders, offering AI-powered chart pattern recognition using advanced OpenCV template matching. It identifies 109 technical analysis patterns in real-time, prioritizing user privacy through on-device processing. The app features predictive detection, multi-modal alerts, pattern invalidation warnings, and explainable AI with audit trails. It operates without subscriptions or cloud dependencies, offering a 4-tier one-time payment structure for lifetime access. Key capabilities include an "Intelligence Stack" comprising the Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules, all functioning offline for educational purposes with legal disclaimers.

## Recent Changes (2025-11-04)
**Professional Onboarding & Overlay-First UX Flow**:
- Created 8-step professional onboarding experience (ProfessionalOnboarding.kt) with premium QUANTRACORE aesthetic: Welcome → Permissions → Detection Power → Intelligence Stack → Voice Alerts → AI Learning → Gamification → Legal Disclaimer
- Implemented overlay-first app flow: after onboarding completion, app auto-launches overlay service and only floating Q button remains visible
- Built robust broadcast acknowledgement system: OverlayService emits "OVERLAY_SERVICE_READY" after successful foreground promotion; MainActivity and auto_launch_overlay wait for confirmation before finishing
- Comprehensive error recovery: 5-second timeout with full error UI showing Retry, Go to Main App, and Check Permissions buttons - zero dead-end scenarios
- Auto_launch_overlay screen handles permission grants with polling system and recovery navigation to dashboard
- MainActivity timeout handling uses FLAG_ACTIVITY_CLEAR_TOP to prevent infinite loops when restarting on failures
- Clicking Q button reopens full MainActivity UI with "opened_from_overlay" flag to distinguish from launcher launches
- All user flows tested: first launch, subsequent launches, permission revocation, persistent service failures, and Q-button entry - all end in overlay or dashboard without dead-ends

**Premium UI Theme Transformation**:
- Upgraded color palette to match QUANTRACORE aesthetic: darker navy background (#0D1B2A), electric cyan (#00E5FF), metallic silver system (Chrome, Gunmetal, DarkSilver), amber/bronze accents (#FF9800, #FFA726, #FFD700)
- Integrated Rajdhani font family (sharp geometric typeface) throughout entire UI for professional, modern appearance
- Added cyan glow effects to hero text (12px blur shadow) for signature glowing aesthetic matching reference design
- All text/container color pairs verified at ≥4.5:1 contrast ratio for accessibility compliance (Material 3 guidelines)
- Complete theme creates premium trading terminal look with metallic accents and crisp typography

**Performance Optimizations for UI Smoothness**:
- Database schema upgraded to v13 with performance indexes on PatternMatch (timestamp, patternName, timeframe), PredictedPattern (timestamp, completionPercent), and InvalidatedPattern (timestamp, patternName)
- Hardware acceleration enabled at app and activity level in AndroidManifest
- Large heap enabled for better memory management during pattern processing
- Config change handling optimized to prevent unnecessary Activity recreations on rotation
- Fixed missing import (androidx.compose.ui.unit.dp) in AppScaffold.kt that caused build failures

**Refined Q Logo & Overlay Positioning**:
- Created refined QuantraVision Q logo with clean, modern design (electric cyan #00E5FF on dark navy #0A1628)
- Updated app icon (ic_launcher_foreground.xml) and floating button (ic_qv_logo_refined.xml) with new logo
- Floating Q button now positioned in very bottom right corner (30px margin) for easy thumb access
- Improved snap-to-edge logic to prefer bottom right corner when dragging
- Verified full-screen touch pass-through: overlay scans entire screen while all touches pass through to trading apps except the Q button

**Premium Visual Polish Enhancements**:
- Implemented breathing cyan glow animation on floating Q button during SCANNING status: pulses from 0% to 60% opacity over 1.5s with AccelerateDecelerateInterpolator
- Glow cleanly stops when scanning exits (PATTERNS_FOUND, HIGH_CONFIDENCE, IDLE states) with proper animator lifecycle management
- Enhanced app launcher icon with multi-layered cyan glow border: 3 concentric layers at 0.12, 0.20, 0.30 opacity creating progressive depth effect
- All animations architect-reviewed for professional quality and production readiness
- Zero memory leaks: all animators properly cancelled and nulled on state transitions

**Build Compilation Fixes (Comprehensive)**:
- Fixed Material 3 Compose API compatibility: replaced 38+ instances of incorrect `tonalElevation` parameter with correct `defaultElevation` parameter in CardDefaults.cardElevation() across 8 screens
  * Round 1 (23 fixes): ProofCapsuleScreen, BehavioralGuardrailsScreen, PatternPlanScreen, RegimeNavigatorScreen, IntelligenceScreen
  * Round 2 (15 fixes): DashboardScreen, SettingsAdvancedScreen, SettingsScreen
- Resolved all unresolved reference errors: added explicit imports for theme colors (ElectricCyan, DarkSurface, CrispWhite, DeepNavyBackground, MetallicSilver, NeonRed) and shadows (CyanGlowShadow, SubtleGlowShadow) to 5 screens in subpackages (LearningDashboardScreen, OnboardingScreen, BookViewerScreen, AdvancedLearningDashboardScreen)
- Added missing standard library imports: mutableStateOf (SettingsScreen), FontWeight (TemplateManagerScreen)
- All Kotlin compilation errors eliminated (38+ total fixes), LSP diagnostics clean, project validation passed
- Build ready for successful APK assembly in GitHub Actions CI/CD pipeline

## User Preferences
Preferred communication style: Simple, everyday language.

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with the Material 3 Design System, optimized for a dark theme, featuring a declarative and reactive UI. It utilizes ViewModel for state management, a modular screen architecture, responsive layouts, custom home screen widgets, and a consistent brand identity with a dark, high-contrast, futuristic aesthetic.

### Technical Implementations
**Pattern Detection Engine**: Employs an OpenCV template matching system for 109 chart patterns, coordinated by a HybridDetectorBridge and utilizing a BayesianFusionEngine for confidence scoring, with TensorFlow Lite infrastructure for future ML enhancements.
**Scan Learning Engine**: Learns from chart scans to track pattern frequency, co-occurrence, and confidence distributions using perceptual image hashing for privacy-preserving, offline learning.
**Data Storage**: Utilizes an encrypted Room database for local storage of logs, user preferences, achievements, and scan learning data.
**State Management**: Implements Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Manages a four-tier lifetime access model via Google Play In-App Billing, secured with Google Play Integrity API.
**AI/ML Architecture**: Primarily OpenCV-based, with TensorFlow Lite infrastructure for future Apache 2.0 licensed ML model integration.
**Alert System**: A centralized AlertManager provides voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay with touch-passthrough to the underlying trading app, featuring a minimal, pulsing cyan border upon pattern detection.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions, with resource optimizations for OpenCV.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, logging detections with SHA-256 hashes, signing pattern catalogs with Ed25519, and maintaining an SBOM.

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
- **Google Play Billing**: In-app purchase handling.
- **Google Play Integrity API**: Anti-tamper verification.

### Offline Assets
- **Pattern Templates**: 109 PNG reference images and YAML configurations.
- **Legal Documents**: HTML/Markdown for terms and privacy policy.
- **Educational Content**: Interactive lessons.

### Security & Compliance
- **Ed25519 Cryptography**: Digital signatures.
- **SHA-256 Hashing**: Integrity verification.
- **Play Integrity API**: Runtime device and app verification.