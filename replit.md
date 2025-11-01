# QuantraVision

## Overview
QuantraVision is a professional Android application that offers advanced AI-powered chart pattern recognition for retail traders. It operates 100% offline, using a hybrid detection system combining YOLOv8 machine learning (6 core patterns) and OpenCV template matching (102 patterns) to identify 108 distinct technical analysis patterns in real-time. The app emphasizes privacy, with all processing occurring on-device, and features include predictive detection, multi-modal alerts, pattern invalidation warnings, and explainable AI with audit trails.

The application targets retail traders who desire professional-grade pattern detection without subscriptions or cloud dependencies. QuantraVision distinguishes itself with a unique "Intelligence Stack" comprising four key features: Regime Navigator (on-device market condition analysis), Pattern-to-Plan Engine (educational trade scenarios), Behavioral Guardrails (discipline coaching), and Proof Capsules (shareable tamper-proof detection receipts). All features are 100% offline and include comprehensive legal disclaimers, emphasizing that they are educational tools and not financial advice.

## User Preferences
Preferred communication style: Simple, everyday language.

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with Material 3 Design System, optimized for a dark theme (#0A1218 background, #00E5FF cyan accent) to reduce eye strain. The UI is declarative and reactive, using ViewModel for state management and a modular screen architecture (Dashboard, Detection, Analytics, Education, Settings, Intelligence Stack). Key design principles include responsive layouts, custom home screen widgets, and a consistent brand identity using the Lamont Labs geometric logo. The Intelligence Stack features dedicated screens (RegimeNavigatorScreen, PatternPlanScreen, BehavioralGuardrailsScreen, ProofCapsuleScreen) with integrated disclaimers and pro-tier gating.

### Technical Implementations
**Pattern Detection Engine**: A hybrid dual-tier system combines YOLOv8 via TensorFlow Lite for 6 core patterns and OpenCV template matching for 102 additional patterns. A HybridDetectorBridge coordinates detection, and a BayesianFusionEngine provides probabilistic confidence scoring. Optimizations like DeltaDetectionOptimizer and TemporalStabilizer enhance performance and stability.
**Data Storage**: Utilizes an encrypted Room database for pattern detection logs, user preferences, and achievement tracking, with no cloud synchronization.
**State Management**: Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) ensure reactive state propagation and prevent memory leaks.
**Authentication & Licensing**: Google Play In-App Billing supports a four-tier structure (Free, Book Add-On, Standard, Pro) with lifetime access. Security measures include Google Play Integrity API integration, signature verification, debugger detection, root checks, and R8/ProGuard obfuscation.
**AI/ML Architecture**: The PyTorch YOLOv8 model (trained on 9,000 chart screenshots) is converted to TensorFlow Lite with INT8 quantization. Optimizations include GPU/NNAPI delegates, TensorPool for memory efficiency, and a PowerPolicyManager to adjust FPS based on battery and thermal states.
**Alert System**: A centralized AlertManager coordinates voice (Android TTS), haptic (pattern-specific vibration), and visual alerts with pattern strength scoring.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay, managed by LiveOverlayController. Feature gating is tier-based, restricting access to patterns and advanced features.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts app performance (FPS, ML usage) based on battery and thermal conditions. Resource optimization includes stride-safe YUV to Bitmap conversion and proper Mat disposal in OpenCV.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, logging every detection with SHA-256 hashes, signing pattern catalogs with Ed25519, and maintaining an SBOM. Extensive legal frameworks (FINANCIAL_DISCLAIMER.md, TERMS_OF_USE.md, INTERNATIONAL_ADDENDUM.md, PRIVACY_POLICY.md) ensure global multi-jurisdictional compliance, emphasizing zero data collection and "illustrative only" disclaimers.

## External Dependencies

### Core ML/CV Libraries
- **TensorFlow Lite**: On-device ML inference for YOLOv8.
- **OpenCV**: Computer vision for template matching and image processing.
- **YOLOv8 Model**: HuggingFace foduucom/stockmarket-pattern-detection-yolov8.

### Android Framework
- **Kotlin**: Primary language.
- **Jetpack Compose**: UI framework.
- **Room**: Local database persistence.
- **Android Architecture Components**: ViewModel, LiveData, WorkManager.
- **Material 3**: Design system.
- **CameraX/MediaProjection**: Screen capture APIs.

### Utilities
- **Gson**: JSON parsing.
- **Google Play Billing**: In-app purchase handling.
- **Google Play Integrity API**: Anti-tamper verification.

### Offline Assets
- **Pattern Templates**: 119 PNG reference images + YAML configurations.
- **Legal Documents**: HTML/Markdown copies of terms, privacy policy, disclaimers.
- **Demo Charts**: Static sample charts.
- **Educational Content**: 25 interactive lessons.
- **Trading Book**: "The Friendly Trader" by Jesse J. Lamont.

### Security & Compliance
- **Ed25519 Cryptography**: Digital signatures.
- **SHA-256 Hashing**: Integrity verification.
- **Play Integrity API**: Runtime device/app verification.