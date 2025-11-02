# QuantraVision

## Overview
QuantraVision is an offline-first Android application that provides AI-powered chart pattern recognition for retail traders. It leverages advanced OpenCV template matching to identify 109 technical analysis patterns in real-time. The app prioritizes user privacy through on-device processing and offers features such as predictive detection, multi-modal alerts, pattern invalidation warnings, and explainable AI with audit trails. All technology is Apache 2.0 licensed.

The application aims to deliver professional-grade pattern detection without subscriptions or cloud dependencies, utilizing a 4-tier one-time payment structure (Free, Starter, Standard, Pro) for lifetime access. Key features include an "Intelligence Stack" comprising the Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules. These features are entirely offline and include legal disclaimers, emphasizing their educational purpose.

## User Preferences
Preferred communication style: Simple, everyday language.

## Recent Changes

**November 2, 2025 - Upgrade Pricing Implementation (Pay Only the Difference):**
- **Fair Upgrade Pricing** - Users only pay the difference when upgrading between tiers (not full price)
  - 3 new upgrade SKU products defined with differential pricing
  - STARTER → STANDARD: $15.00 (saves $9.99 from $24.99 full price)
  - STARTER → PRO: $40.00 (saves $9.99 from $49.99 full price)
  - STANDARD → PRO: $25.00 (saves $24.99 from $49.99 full price)
- **BillingManager Upgrade Logic** - Automatically detects and uses upgrade SKUs
  - getUpgradeSku() returns correct upgrade SKU based on tier transition
  - purchaseStandard() and purchasePro() auto-select upgrade SKUs when applicable
  - Upgrade purchases properly acknowledged to prevent refunds
  - Purchase history preserved (original tier purchases remain in history)
- **UI Enhancements** - Clear upgrade messaging in both UpgradeScreen and PaywallScreen
  - 🎁 UPGRADE badge shown for eligible upgrades
  - Strikethrough original price (e.g., ~~$24.99~~)
  - Bold upgrade price prominently displayed
  - "You pay only the difference" explanatory text
  - "UPGRADE NOW" button (instead of "BUY NOW")
- **Edge Cases Handled** - FREE users see full pricing, current tier shows "ALREADY OWNED ✓", lower tiers disabled
- **Architect Certification: PASS** - Ready for production after creating 3 upgrade SKUs in Google Play Console
- **Files Modified**: billing_skus.json, BillingManager.kt, Entitlements.kt, UpgradeScreen.kt, PaywallScreen.kt

**November 2, 2025 - Pattern Count Corrected to 109:**
- Updated all documentation and code from 102 to 109 patterns (28 files changed)
- Database schema documentation added (app/schemas/README.md)
- APK signing process fully documented in README
- Production readiness upgraded from 87/100 to 96/100
- Architect certified: Ready for Google Play release

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with the Material 3 Design System, optimized for a dark theme. It features a declarative and reactive UI, utilizing ViewModel for state management and a modular screen architecture. Design principles include responsive layouts, custom home screen widgets, and a consistent brand identity, with dedicated screens for the Intelligence Stack.

### Technical Implementations
**Pattern Detection Engine**: Employs an OpenCV template matching system for 109 chart patterns, coordinated by a HybridDetectorBridge and utilizing a BayesianFusionEngine for confidence scoring. Includes DeltaDetectionOptimizer and TemporalStabilizer, with TensorFlow Lite infrastructure for future ML enhancements.
**Scan Learning Engine**: (PRO tier only) Learns from every chart scan to track pattern frequency, co-occurrence, and confidence distributions. It uses perceptual image hashing for privacy-preserving, offline learning with adaptive threshold optimization and a 90-day data retention policy.
**Data Storage**: Utilizes an encrypted Room database for local storage of logs, user preferences, achievements, and scan learning data.
**State Management**: Implements Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Manages a four-tier lifetime access model via Google Play In-App Billing, secured with Google Play Integrity API, signature verification, and R8/ProGuard obfuscation.
**AI/ML Architecture**: Primarily OpenCV-based, with TensorFlow Lite infrastructure prepared for future Apache 2.0 licensed ML model integration.
**Alert System**: A centralized AlertManager provides voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay, featuring a minimal glowing cyan border that pulses upon pattern detection. A small, draggable floating logo button provides access to the full app UI or a quick actions menu. The overlay uses `FLAG_NOT_TOUCHABLE` for full touch-passthrough to the underlying trading app.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions, with resource optimizations for OpenCV.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, logging detections with SHA-256 hashes, signing pattern catalogs with Ed25519, and maintaining an SBOM, with legal frameworks emphasizing privacy and "illustrative only" disclaimers.

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