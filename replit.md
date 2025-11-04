# QuantraVision

## Overview
QuantraVision is an offline-first Android application for retail traders, providing AI-powered chart pattern recognition using OpenCV template matching. It identifies 109 technical analysis patterns in real-time, prioritizing user privacy through on-device processing. Key features include predictive detection, multi-modal alerts, pattern invalidation warnings, and explainable AI with audit trails. The app operates without subscriptions or cloud dependencies, offering a 4-tier one-time payment for lifetime access. It includes an "Intelligence Stack" (Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, Proof Capsules) for educational purposes, all functioning offline.

## User Preferences
Preferred communication style: Simple, everyday language.

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with the Material 3 Design System, optimized for a dark theme, featuring a declarative and reactive UI. It utilizes ViewModel for state management, a modular screen architecture, responsive layouts, and a consistent brand identity with a dark, high-contrast, futuristic aesthetic, including the Rajdhani font family and cyan glow effects. The UI is designed for professional interaction, including an overlay-first UX and a refined Q logo.

### Technical Implementations
**Pattern Detection Engine**: Employs an OpenCV template matching system for 109 chart patterns, coordinated by a HybridDetectorBridge and utilizing a BayesianFusionEngine for confidence scoring. TensorFlow Lite infrastructure is in place for future ML enhancements.
**Scan Learning Engine**: Learns from chart scans to track pattern frequency, co-occurrence, and confidence distributions using perceptual image hashing for privacy-preserving, offline learning.
**Data Storage**: Utilizes an encrypted Room database for local storage of logs, user preferences, achievements, and scan learning data.
**State Management**: Implements Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Manages a four-tier lifetime access model via Google Play In-App Billing, secured with Google Play Integrity API.
**Alert System**: A centralized AlertManager provides voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay with touch-passthrough to the underlying trading app, featuring a minimal, pulsing cyan border upon pattern detection.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions, with resource optimizations for OpenCV and hardware acceleration.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, logging detections with SHA-256 hashes, signing pattern catalogs with Ed25519, and maintaining an SBOM.
**Onboarding**: Features an 8-step professional onboarding experience and an overlay-first app flow for subsequent launches.

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