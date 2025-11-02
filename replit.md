# QuantraVision

## Overview
QuantraVision is a professional Android application providing AI-powered chart pattern recognition for retail traders. It operates 100% offline, utilizing advanced OpenCV template matching to identify 102 technical analysis patterns in real-time. The app prioritizes privacy with on-device processing and offers features like predictive detection, multi-modal alerts, pattern invalidation warnings, and explainable AI with audit trails. All technology is Apache 2.0 licensed.

The application aims to offer professional-grade pattern detection without subscriptions or cloud dependencies. QuantraVision includes an "Intelligence Stack" featuring: Regime Navigator (on-device market condition analysis), Pattern-to-Plan Engine (educational trade scenarios), Behavioral Guardrails (discipline coaching), and Proof Capsules (shareable tamper-proof detection receipts). All features are offline and include legal disclaimers, emphasizing their educational nature.

## User Preferences
Preferred communication style: Simple, everyday language.

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with Material 3 Design System, optimized for a dark theme to reduce eye strain. The UI is declarative and reactive, using ViewModel for state management and a modular screen architecture (Dashboard, Detection, Analytics, Education, Settings, Intelligence Stack). Key design principles include responsive layouts, custom home screen widgets, and a consistent brand identity. The Intelligence Stack features dedicated screens with integrated disclaimers and pro-tier gating.

### Technical Implementations
**Pattern Detection Engine**: Employs an OpenCV template matching system to detect 102 chart patterns using reference image templates. A HybridDetectorBridge coordinates detection, and a BayesianFusionEngine provides probabilistic confidence scoring. Optimizations include DeltaDetectionOptimizer and TemporalStabilizer. TensorFlow Lite infrastructure is present for future Apache 2.0 licensed ML enhancements.
**Data Storage**: Utilizes an encrypted Room database for logs, user preferences, and achievement tracking, with no cloud synchronization.
**State Management**: Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) ensure reactive state propagation.
**Authentication & Licensing**: Google Play In-App Billing supports a four-tier structure (Free, Book Add-On, Standard, Pro) with lifetime access. Security includes Google Play Integrity API integration, signature verification, debugger detection, root checks, and R8/ProGuard obfuscation.
**AI/ML Architecture**: Primarily uses OpenCV template matching. TensorFlow Lite infrastructure is ready for future Apache 2.0 licensed ML model integration, with optimizations like TensorPool and PowerPolicyManager.
**Alert System**: A centralized AlertManager coordinates voice (Android TTS), haptic, and visual alerts with pattern strength scoring.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay, managed by LiveOverlayController, with tier-based feature gating.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts app performance based on battery and thermal conditions. Resource optimization includes stride-safe YUV to Bitmap conversion and proper Mat disposal in OpenCV.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, logging detections with SHA-256 hashes, signing pattern catalogs with Ed25519, and maintaining an SBOM. Extensive legal frameworks ensure global multi-jurisdictional compliance, emphasizing zero data collection and "illustrative only" disclaimers.

## External Dependencies

### Core ML/CV Libraries
- **OpenCV**: Computer vision for template matching and image processing (Apache 2.0 licensed).
- **TensorFlow Lite**: Infrastructure for future ML enhancements (Apache 2.0 licensed).

### Android Framework
- **Kotlin**: Primary language.
- **Jetpack Compose**: UI framework.
- **Room**: Local database persistence.
- **Android Architecture Components**: ViewModel, LiveData, WorkManager.
- **Material 3**: Design system.
- **MediaProjection**: Screen capture APIs for live overlay.

### Utilities
- **Gson**: JSON parsing.
- **Google Play Billing**: In-app purchase handling.
- **Google Play Integrity API**: Anti-tamper verification.

### Offline Assets
- **Pattern Templates**: 102 PNG reference images + YAML configurations (Apache 2.0 licensed).
- **Legal Documents**: HTML/Markdown copies of terms, privacy policy, disclaimers.
- **Educational Content**: Interactive lessons.

### Security & Compliance
- **Ed25519 Cryptography**: Digital signatures.
- **SHA-256 Hashing**: Integrity verification.
- **Play Integrity API**: Runtime device/app verification.