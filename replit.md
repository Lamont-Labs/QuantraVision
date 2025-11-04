# QuantraVision - AI Pattern Detection for Traders

## Overview

QuantraVision is a professional Android application that provides real-time chart pattern detection using computer vision and machine learning. Built with privacy-first principles, the app operates 100% offline, running all AI/ML computations on-device without collecting or transmitting user data.

The application serves as an educational tool for technical analysis, detecting 109 chart patterns across multiple timeframes and providing visual overlays, voice announcements, and haptic feedback. It follows a freemium model with 3-5 free pattern highlights, then offers Standard ($9.99) and Pro ($24.99) one-time purchase tiers.

**Core Purpose:** Democratize professional-grade technical analysis through privacy-first, offline AI pattern detection that rivals institutional-level tools.

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Application Architecture Pattern

**Clean Architecture with MVVM** - The application follows a three-layer architecture with clear separation of concerns:

1. **Presentation Layer** - Jetpack Compose UI with Material 3 design system, ViewModels managing UI state, custom theme with Lamont Labs branding (#0A1218 background, #00E5FF cyan accent)

2. **Domain Layer** - Business logic including pattern detection algorithms, educational content, gamification systems, and billing management

3. **Data Layer** - Room database for local persistence, SharedPreferences for settings, encrypted storage for billing data

**Design Rationale:** Clean architecture ensures testability, maintainability, and clear boundaries between UI, business logic, and data access. MVVM pattern provides reactive UI updates and lifecycle-aware state management.

### Pattern Detection Pipeline

**Hybrid Detection System** combining computer vision and machine learning:

- **Template Matching (OpenCV)** - 119 reference template images for 109 deterministic chart patterns, multi-scale detection with configurable thresholds (0.70-0.80), temporal stability filtering to prevent flickering
- **Machine Learning Enhancement (TensorFlow Lite)** - Bayesian fusion for confidence calibration, pattern similarity search, early formation detection (40-85% complete)
- **5-Phase Optimization Framework** - PowerPolicy for adaptive performance based on battery/thermal state, model pooling to reduce memory overhead, intelligent caching

**Design Rationale:** Template matching provides deterministic, reproducible results essential for educational trust. ML enhancement adds probabilistic refinement without sacrificing transparency. Hybrid approach balances accuracy with on-device performance constraints.

### Real-Time Overlay System

**MediaProjection-based overlay** drawing pattern highlights over any chart application:

- **LiveOverlayController** - Manages VirtualDisplay creation, ImageReader for frame capture, stride-safe YUV→Bitmap conversion to prevent crashes
- **EnhancedOverlayView** - Custom View with 7-layer rendering (shadow, glow, border, fill, corner accents, labels, confidence badges), zero per-frame allocations (13 cached Paints), 60 FPS capable
- **HighlightGate** - Quota management system enforcing free tier limits (3-5 highlights), pattern deduplication via quantized position IDs

**Design Rationale:** MediaProjection API enables universal chart compatibility without requiring integration with specific trading platforms. Custom rendering with object pooling ensures smooth 60 FPS performance. Quota system protects freemium business model while providing meaningful free trial.

### Data Persistence Strategy

**Room Database (11 entities)** - Local storage for pattern outcomes, achievements, learning metadata, confidence profiles, suppression rules, pattern correlations

**EncryptedSharedPreferences** - Billing data (purchase status, license tier) with fail-closed security pattern (throws SecurityException if encryption initialization fails, no plaintext fallback)

**Scoped Storage** - Proof exports and PDF reports stored in app-internal directory, auto-cleanup with configurable TTL

**Design Rationale:** Room provides type-safe database access with migration support. Encrypted storage protects sensitive billing data from compromise. Scoped storage complies with Android 10+ privacy requirements while maintaining export functionality.

### Educational System Architecture

**Modular Lesson Structure** - 25 interactive lessons refactored from monolithic 6,714-line file into 28 modular files (<500 lines each), LessonRegistry aggregator pattern for centralized access, shared data models for consistency

**Gamification Engine** - 15 achievements with unlock conditions, daily streak tracking, pattern performance statistics, user progress analytics

**Pattern-to-Plan Engine** - Generates hypothetical trade scenarios (entry/exit points, stop losses, position sizing) with extensive legal disclaimers clarifying educational-only purpose

**Design Rationale:** Modular lesson architecture improves maintainability and IDE performance. Gamification increases engagement and learning retention. Hypothetical scenarios teach practical application while legal disclaimers protect against misuse as financial advice.

### Multi-Modal Alert System

**Centralized AlertManager singleton** - Thread-safe with double-checked locking, coordinates voice (Android TTS), haptic (pattern-specific vibrations: 2 buzzes=bullish, 3=bearish, long=high confidence), and visual alerts

**Voice Announcements** - Natural language pattern alerts ("Head and Shoulders forming - 75% complete, strong confidence"), hands-free operation for active trading, user-configurable enable/disable

**Haptic Feedback** - Multi-modal vibration patterns providing glanceable confirmation, pattern type and confidence level encoded in vibration sequence

**Design Rationale:** Centralized manager prevents alert conflicts and ensures consistent behavior. Multi-modal alerts accommodate different usage contexts (eyes-free via voice/haptic, visual for quiet environments). Singleton pattern with application context prevents memory leaks.

### Billing & Licensing System

**Google Play Billing Library 7.x** - One-time purchases (no subscriptions), secure purchase verification, encrypted local storage of license state

**Offline License Validation** - Cached license status enables offline operation, fail-closed pattern (app blocks overlay if license validation fails), periodic refresh when online

**Feature Gating** - Free tier quota (3-5 highlights), Standard tier (unlimited highlights), Pro tier (advanced features: PDF reports, backtesting, multi-chart comparison, pattern similarity search)

**Design Rationale:** One-time purchases align with user preference against subscriptions. Offline license caching maintains functionality without constant network checks. Tiered feature gating provides clear upgrade incentives while preserving core value in free tier.

### Security Architecture

**Fail-Closed Principles** - Encryption failures throw exceptions (no plaintext fallback), MediaProjection errors trigger graceful cleanup with user guidance, signature verification blocks overlay service if tampered

**ProGuard/R8 Obfuscation** - Comprehensive keep rules for ML Kit and OpenCV, release builds fully obfuscated

**Privacy Guarantees** - No network permissions except Play Billing, all processing on-device, no analytics or crash reporting, GDPR/CCPA compliant by design (no data collection)

**Design Rationale:** Fail-closed security prevents silent degradation into insecure states. Obfuscation protects intellectual property. Privacy-first design eliminates regulatory compliance overhead while building user trust.

## External Dependencies

### Android Platform

- **Minimum SDK:** API 26 (Android 8.0 Oreo)
- **Target SDK:** API 34 (Android 14)
- **Compile SDK:** API 34
- **Jetpack Compose:** 1.7.5 with Material 3 design system
- **Java Toolchain:** JDK 17 (GraalVM 22.3 or Temurin)

### Computer Vision & Machine Learning

- **OpenCV:** 4.8.0 (BSD-3-Clause license) - Template matching, image preprocessing, multi-scale detection
- **TensorFlow Lite:** 2.14.0 (Apache-2.0 license) - On-device inference for pattern similarity and early detection
- **ML Kit (future consideration)** - Explored for potential text recognition in indicator legends

### Data & Persistence

- **Room:** 2.6.x (Apache-2.0 license) - Local SQLite database with type-safe queries and migration support
- **Gson:** 2.10 (Apache-2.0 license) - JSON serialization for pattern templates and configuration files
- **EncryptedSharedPreferences (Jetpack Security)** - AES256-GCM encryption for sensitive billing data

### Build & Development

- **Gradle:** 8.7 with Kotlin DSL
- **Kotlin:** 1.9.x
- **KSP (Kotlin Symbol Processing)** - Room annotation processing
- **Android Gradle Plugin:** 8.7.3
- **ProGuard/R8** - Code obfuscation and optimization for release builds

### Payment Processing

- **Google Play Billing Library:** 7.x - In-app purchases (one-time, non-consumable products)

### Testing & Quality

- **JUnit 4** - Unit testing framework
- **Espresso** - UI testing (planned)
- **Validation Framework** - Custom accuracy testing with ground truth datasets

### Key Architectural Decisions

**No Backend Dependency:** All features operate offline except Google Play Billing, reducing operational costs and privacy concerns while ensuring functionality in poor network conditions.

**Template-First Detection:** OpenCV template matching chosen over pure ML for determinism and reproducibility, essential for educational credibility and user trust.

**Modular Refactoring:** Large monolithic files (6,714 lines) refactored into <500 line modules for improved maintainability, IDE performance, and collaborative development.

**Encrypted Billing Storage:** Fail-closed security pattern prevents revenue loss from corrupted purchase records while protecting user payment data.

**MediaProjection API:** Enables universal chart overlay without requiring partnerships or integrations with specific trading platforms, maximizing addressable market.