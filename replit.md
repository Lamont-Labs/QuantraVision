# QuantraVision

## Overview

QuantraVision is a professional Android application providing advanced AI-powered chart pattern recognition for retail traders. The app operates 100% offline using hybrid detection combining YOLOv8 machine learning (6 core patterns) with OpenCV template matching (102 additional patterns) to identify 108 distinct technical analysis patterns in real-time. Designed with privacy-first principles, all processing occurs on-device with zero data collection or network transmission.

The application targets retail traders seeking professional-grade pattern detection without subscriptions or cloud dependencies. Key value propositions include predictive detection at 40-85% pattern formation, multi-modal alerts (voice, haptic, visual), pattern invalidation warnings, and explainable AI with full audit trails.

**Unique Intelligence Stack (v1.0.0):** QuantraVision is differentiated from ALL competitors by 4 game-changing features: (1) Regime Navigator - on-device market condition analysis that tells traders WHEN patterns matter, (2) Pattern-to-Plan Engine - educational trade scenarios with entry/exit/risk calculations, (3) Behavioral Guardrails - discipline coaching that prevents emotional trading, and (4) Proof Capsules - shareable tamper-proof detection receipts for viral growth. All features are 100% offline and include comprehensive legal disclaimers (educational tools only, NOT financial advice).

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Frontend Architecture

**UI Framework**: Jetpack Compose with Material 3 Design System
- Dark theme optimized (#0A1218 background, #00E5FF cyan accent)
- Declarative reactive UI with ViewModel state management
- Modular screen architecture: Dashboard, Detection, Analytics, Education, Settings
- Responsive layouts supporting various Android device sizes
- Custom home screen widget for quick access

**Key Design Decisions**:
- Material 3 chosen for modern Android design consistency and component library
- Dark theme as primary due to trader preference for reduced eye strain during extended chart analysis
- Compose selected over XML layouts for declarative paradigm and easier maintenance
- Lamont Labs geometric logo (Q+V) used for consistent brand identity

### Backend Architecture

**Pattern Detection Engine**: Hybrid dual-tier system
- **Tier 1 (ML)**: YOLOv8 model via TensorFlow Lite detecting 6 core patterns (Head & Shoulders, Triangles, Double Top/Bottom, Trend Lines) with 93.2% mAP@0.5 accuracy
- **Tier 2 (CV)**: OpenCV template matching against 119 reference images for 102 additional patterns
- **Integration Layer**: HybridDetectorBridge coordinates ML and template-based detection with BayesianFusionEngine for probabilistic confidence scoring
- **Optimization**: DeltaDetectionOptimizer uses perceptual hashing for frame-skipping on static charts (40% speedup target)
- **Stability**: TemporalStabilizer applies 5-frame consensus voting to eliminate detection flickering

**Architecture Rationale**: Hybrid approach chosen because ML models excel at chart-agnostic detection (works across platforms/themes) but are limited to patterns with sufficient training data. Template matching provides broad pattern coverage (108 total) but requires standardized chart formats. Combining both provides maximum accuracy and coverage.

**Data Storage**: Room database with encrypted local storage
- Pattern detection logs with timestamps, confidence scores, and provenance chains
- User preferences and watchlist data
- Achievement/gamification progress tracking
- No cloud synchronization - all data remains on-device

**State Management**: Android Architecture Components
- ViewModels hold UI state and coordinate business logic
- Repository pattern abstracts data access between Room DB and detection engines
- LiveData/Flow for reactive state propagation to UI
- Lifecycle-aware components prevent memory leaks

### Authentication & Licensing

**Billing System**: Google Play In-App Billing with three-tier structure
- **Free Tier**: 3-5 pattern highlights (quota-limited), 30 core patterns, watermarked overlays
- **Standard ($9.99)**: Unlimited highlights, 30 core patterns, remove watermarks, PDF export, all 25 lessons
- **Pro ($24.99)**: All Standard features + full 108-pattern library + voice alerts + haptic feedback + predictive detection + backtesting
- HighlightGate enforces highlight quota for Free tier
- PatternLibraryGate restricts pattern access by tier (30 core patterns for Free/Standard, all 108 for Pro)
- BillingManager validates purchases via Google Play and stores entitlements in encrypted SharedPreferences
- StandardFeatureGate and ProFeatureGate read from secure encrypted prefs (cannot be spoofed)
- No subscription model - lifetime access with single purchase

**Security Architecture**:
- IntegrityValidator interfaces with Google Play Integrity API for anti-tamper
- SignatureVerifier validates Lamont Labs release key fingerprint
- DebuggerDetection monitors for debugging attempts
- RootCheck detects common root indicators
- Fail-closed principle: blocks overlay if verification fails
- R8 + ProGuard obfuscation with custom rules for release builds

### AI/ML Architecture

**Model Pipeline**:
- PyTorch YOLOv8 model (84 MB) trained on 9,000 real trading chart screenshots
- TensorFlow Lite conversion target with INT8 quantization (22 MB goal, 74% reduction)
- OptimizedModelLoader supports GPU/NNAPI delegates for hardware acceleration
- TensorPool manages memory-efficient tensor reuse (36% RAM reduction target)

**Performance Optimizations**:
- Current: ~20ms inference per frame at 500 MB RAM
- Target: ≤8ms inference at <350 MB RAM with quantized models
- PowerPolicyManager adjusts FPS (10/20/30/60) based on battery and thermal state
- Thermal throttling at >65°C to prevent device overheating

**Alert System**:
- Centralized AlertManager singleton coordinates voice (Android TTS), haptic (pattern-specific vibration), and visual alerts
- Voice announcements: "Head and Shoulders forming - 75% complete, strong confidence"
- Haptic patterns: 2 buzzes (bullish), 3 buzzes (bearish), long buzz (high confidence), double long (invalidated)
- Pattern strength scoring: Weak (40-60%), Moderate (60-80%), Strong (80-100%) with color coding

### Real-Time Overlay System

**Screen Capture**: MediaProjection API for live chart overlay
- SYSTEM_ALERT_WINDOW permission for overlay drawing
- FOREGROUND_SERVICE for persistent operation
- LiveOverlayController manages frame capture and detection loop
- Deterministic rendering with provenance logging for each detection

**Feature Gating**: Tier-based feature access with billing verification
- **Free → Standard**: Unlimited highlights (vs 3-5 quota), remove watermarks, PDF export
- **Standard → Pro**: Full 108-pattern library (vs 30 core), voice alerts, haptic feedback, predictive detection, pattern invalidation warnings, auto-scanning watchlist, backtesting engine with CSV import, proof bundle export with SHA-256 hashes and Ed25519 signatures
- PatternLibraryGate.STANDARD_TIER_PATTERNS defines the 30 core patterns (Head & Shoulders, Double Top/Bottom, Triangles, Flags, Pennants, basic candlesticks)
- HighlightGate applies two-step filtering: (1) tier-based pattern filtering, (2) quota-based highlight limiting for Free tier

### Performance & Power Management

**Adaptive Pipeline**: PowerPolicyApplicator runs background monitoring every 5 seconds
- Ultra Low Power (<15% battery): 10 FPS, ML disabled, template-only
- Balanced (15-30% battery): 20 FPS, ML enabled, reduced scales
- High Performance (>30% battery): 30-60 FPS, full pipeline
- Thermal throttling engages above 65°C

**Resource Optimization**:
- Stride-safe YUV to Bitmap conversion prevents crashes
- Proper Mat disposal in OpenCV to prevent memory leaks
- Asynchronous I/O operations on Dispatchers.IO
- ProGuard keep rules for ML Kit and OpenCV in release builds

### Compliance & Provenance

**Determinism**: Greyline OS v4.3 compliance standard
- Every detection logged with SHA-256 hash of input frame
- Pattern template catalog hashed and signed with Ed25519
- SBOM (Software Bill of Materials) tracks all dependencies
- verify.sh script validates integrity chain from source to binary

**Legal Framework**: Comprehensive disclaimer system
- Financial disclaimer: NOT financial advice, NOT FINRA/SEC registered, trading risks disclosed
- Terms of use: Educational tool only, liability capped at purchase price ($24.99), arbitration clause, California jurisdiction
- Privacy policy: Zero data collection, 100% offline, CCPA-compliant
- Mandatory disclaimer acceptance in onboarding flow
- Watermark on all overlays: "⚠ Illustrative Only — Not Financial Advice"

## External Dependencies

### Core ML/CV Libraries
- **TensorFlow Lite** (2.14+, Apache-2.0): On-device ML inference for YOLOv8 model
- **OpenCV** (4.8, BSD-3-Clause): Computer vision template matching and image processing
- **YOLOv8 Model**: HuggingFace foduucom/stockmarket-pattern-detection-yolov8 (trained on 9,000 trading charts)

### Android Framework
- **Kotlin** (2.1.0): Primary language
- **Jetpack Compose** (1.6, Apache-2.0): UI framework
- **Room** (2.6, Apache-2.0): Local database persistence
- **Android Architecture Components**: ViewModel, LiveData, WorkManager
- **Material 3**: Design system and components
- **CameraX/MediaProjection**: Screen capture APIs

### Utilities
- **Gson** (2.10, Apache-2.0): JSON parsing for pattern templates and configuration
- **Google Play Billing** (Apache-2.0): In-app purchase handling
- **Google Play Integrity API**: Anti-tamper verification

### Build Tools
- **Gradle** (8.11.1): Build system via wrapper
- **Android SDK**: API 35 (Android 15), minimum API 26 (Android 8.0)
- **JDK**: 17 or higher (GraalVM 22.3 in Replit environment)
- **R8/ProGuard**: Code shrinking and obfuscation

### Development Services
- **Google Play Console**: App distribution and billing configuration
- **Android Studio**: Ladybug (2024.2.1) IDE for development
- **Replit**: Cloud IDE with automated Android SDK setup via scripts

### Offline Assets
- **Pattern Templates**: 119 PNG reference images + YAML configurations
- **Legal Documents**: HTML/Markdown copies of terms, privacy policy, disclaimers
- **Demo Charts**: Static sample charts for offline testing
- **Educational Content**: 25 interactive lessons with quizzes

### Security & Compliance
- **Ed25519 Cryptography**: Digital signatures for provenance chains
- **SHA-256 Hashing**: Integrity verification for builds and templates
- **Play Integrity API**: Runtime device/app verification

**Database**: Room (SQLite) - no external database services required
**Analytics**: None - zero telemetry or tracking
**Crash Reporting**: Local-only crash logs, 7-day auto-deletion
**Cloud Storage**: None - 100% offline operation
**Network APIs**: None - INTERNET permission explicitly omitted from manifest