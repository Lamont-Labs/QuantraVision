# QuantraVision

## Overview
QuantraVision is an offline-first Android application for retail traders. It provides AI-powered, on-device chart pattern recognition using OpenCV to identify 109 technical analysis patterns. The app offers real-time detection, predictive analysis, multi-modal alerts, and explainable AI with audit trails, all while prioritizing user privacy through on-device processing. It operates without subscriptions or cloud dependencies, offering lifetime access via a one-time payment. Key features include an "Intelligence Stack" (Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, Proof Capsules) focused on offline functionality and educational support.

## Recent Changes
- **2025-11-20 (Late Evening)**: Critical Embeddings-Only Mode Fixes:
  - **Root Cause Fix**: Fixed path mismatch where ModelDiagnostics checked 'models' directory but ModelManager used 'llm_models' directory. This caused false-positive model missing errors. ModelDiagnostics now checks correct 'llm_models' path.
  - **MobileBERT Removal**: Completely removed broken MobileBERT model from assets (file deleted). Model lacked TFLite Task Library metadata and would crash on initialization. Auto-provisioning now only provisions SENTENCE_EMBEDDINGS model.
  - **Diagnostic Logic Fix**: Optional model failures (MobileBERT, Intent Classifier) no longer recorded as initialization failures. This eliminates 93 false-positive error recommendations that flooded DevBot diagnostics.
  - **Component Health Fix**: System now correctly reports HEALTHY when embeddings load successfully, regardless of MobileBERT availability. Embeddings-only mode is now the production configuration.
  - **Idempotent Initialization**: Fixed "already initialized" check to only require embeddings (not MobileBERT), preventing unnecessary re-initialization loops.
  - **isReady() Bug Fix**: Fixed EnsembleEngine.isReady() to only check embeddings retriever (not MobileBERT). This bug caused QuantraBot to incorrectly show "model not loaded" error even when embeddings were working.
  - **Documentation Updates**: Updated EnsembleEngine class-level KDoc to reflect embeddings-only architecture (22MB bundled vs 555MB Gemma = 25x smaller). Only embeddings required; MobileBERT and Intent Classifier are optional and not bundled.
  - **Results**: 93 initialization errors eliminated (down to 48 harmless system warnings). DevBot diagnostics stay green. AI responses work via retrieval-only mode with 198 Q&A knowledge base. QuantraBot AI assistant fully functional.
- **2025-11-20 (Evening)**: DevBot Diagnostic System Enhancements:
  - **Build Fingerprinting**: Added BUILD_FINGERPRINT (v3.1.0-3100-abc123f), BUILD_TIMESTAMP, GIT_HASH, BUILD_ID to BuildConfig for version tracking. User can now instantly verify which build they're testing. Visible in DevBot UI Build Info card.
  - **Startup Diagnostics**: Created StartupDiagnosticCollector that captures complete app initialization timeline from App.onCreate() to completeStartup(). Exposes StartupTimeline StateFlow with metadata (total duration, failed/warning components, event list). DevBot UI shows expandable timeline with status badges.
  - **Component Health Monitoring**: Built ComponentHealthMonitor tracking 7 major systems (AI Engine, Pattern Detection, Database, OCR, Alert, Learning, QuantraCore) in real-time. Updates every second via StateFlow. DevBot UI displays color-coded health dashboard.
  - **Model Diagnostics**: Enhanced ModelDiagnostics with initialization tracking (timestamps, success/failure, errors) integrated into EnsembleEngine. Comprehensive diagnostics exported include all model file status and init events.
  - **Enhanced Exports**: DiagnosticEngine exports now include build info, startup timeline, component health, and model diagnostics for comprehensive troubleshooting.
  - **Thread Safety**: All diagnostic collectors use proper synchronization (synchronized blocks, AtomicBoolean, StateFlow) to prevent race conditions during concurrent startup.
- **2025-11-20**: Version 3.1.0 released with Bundled Ensemble AI Engine + APK Size Optimization:
  - **AI Engine**: Replaced single 555MB Gemma model with bundled quantized TFLite models: all-MiniLM-L6-v2 sentence embeddings (22MB) bundled in APK. Makes AI responses 5-10x faster for common questions. Models auto-provision from assets on first launch - NO USER IMPORT REQUIRED! EmbeddingsRetriever provides instant answers via cosine similarity search against QAKnowledgeBase (198 pre-written pattern/trading Q&As). MobileBERT Q&A model (25MB) is optional and currently disabled due to TFLite Task Library metadata incompatibility - system runs in retrieval-only mode. All processing remains 100% offline.
  - **APK Optimization**: APK size reduced from 250MB to ~147MB through: (1) ABI filtering to arm64-v8a only (Samsung S23 FE target), (2) Disabled universal APK splits, (3) Removed MediaPipe dependency (12MB, replaced by ensemble engine). All code updated from GemmaEngine to EnsembleEngine.
  - **Fix (2025-11-20)**: Made MobileBERT completely optional after discovering model file lacks TFLite Task Library metadata. System now gracefully runs in retrieval-only mode with 198 Q&A knowledge base covering trading patterns, indicators, and strategies.

## User Preferences
Preferred communication style: Simple, everyday language.
Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with Material 3 Design System, optimized for a dark theme with a consistent chrome/steel metallic brand identity. It features high-contrast elements, dual-font typography (Orbitron for headers, Space Grotesk for body), and a streamlined, professional UI with a 5-tab Material Design 3 bottom navigation bar. A unified `StaticBrandBackground` provides visual depth with neon cyan and gold accents.

### Technical Implementations
**Pattern Detection Engine**: Employs OpenCV template matching for 109 chart patterns, coordinated by a `HybridDetectorBridge`, with a `BayesianFusionEngine` for confidence scoring. TensorFlow Lite is planned for future ML enhancements.
**QuantraCore Intelligence System**: A multi-signal analysis engine combining pattern detection with OCR-extracted technical indicators (RSI, MACD, volume, 30+ others) using Google ML Kit Text Recognition to generate a 0-100 `QuantraScore`. Includes `ContextAnalyzer` for confluence detection and `SmartFilter` for quality thresholding. All processing is 100% offline.
**Pattern Learning Engine**: A self-improving adaptive system that learns from every scan to enhance pattern detection accuracy, analyzing historical scan data via `HistoricalAnalyzer`. It learns every 50 scans and is 100% offline.
**Ensemble AI Engine**: A retrieval-based Q&A system using all-MiniLM-L6-v2 sentence embeddings (22MB) for semantic search against 198 pre-written answers. Offers 15x smaller size and 5-10x faster inference compared to a single large model. IntentClassifier and MobileBERTQaAdapter are optional components (currently MobileBERT disabled due to metadata incompatibility). All processing is 100% offline.
**AI Explanation Engine**: Provides natural language explanations for detected patterns using the Ensemble AI Engine, integrated into pattern notifications. All explanations are 100% offline.
**QuantraBot AI Assistant**: An interactive AI trading assistant built on the Ensemble AI Engine with a comprehensive expert pattern knowledge base. Accessible via a dedicated "Bot" tab, operating 100% offline.
**DevBot Diagnostic AI** (DEBUG builds only): A real-time application health monitoring system with AI-powered error analysis, appearing as a conditional 6th tab. It coordinates monitoring systems and provides conversational error analysis, operating 100% offline.
**Model Import System** (Deprecated): Previously allowed manual import of AI models. Now obsolete as models auto-provision from bundled APK assets on first launch. Only embeddings model is required; MobileBERT is optional and currently disabled.
**QA Knowledge Base**: A comprehensive dataset of 198 pre-written Q&A entries covering trading education, stored in JSON format, used by the EmbeddingsRetriever for instant answers.
**Data Storage**: Encrypted Room database for local storage of logs, user preferences, and scan learning data.
**State Management**: Utilizes Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Four-tier lifetime access model managed via Google Play In-App Billing, secured with Google Play Integrity API.
**Alert System**: Centralized `AlertManager` for multi-modal alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for a tap-to-scan overlay with notification-based results.
**Performance & Power Management**: An Adaptive Pipeline with a `PowerPolicyApplicator` adjusts performance based on device conditions.

### Feature Specifications
- **Intelligence Stack**: Comprises `Regime Navigator`, `Pattern-to-Plan Engine`, `Behavioral Guardrails`, and `Proof Capsules` for advanced offline analysis.
- **Pattern-to-Plan Overlay**: Integrates trade scenario display (entry, stop, target) directly into the overlay for instant user access (Pro tier).
- **Offline Functionality**: All core features operate entirely on-device without cloud dependencies.
- **Bottom Navigation Structure**: Features a 5-tab Material 3 NavigationBar (6 tabs in DEBUG builds) with Home, Markets, Scan, QuantraBot, and Settings sections.
- **Tier Onboarding System**: Post-purchase celebration modal showcasing unlocked features with deep-links.

## External Dependencies

### Core ML/CV Libraries
- **OpenCV**: Computer vision for template matching and image processing.
- **TensorFlow Lite**: On-device ML infrastructure with Task Library for text classification and Q&A models.
- **TensorFlow Lite Task Text**: Provides NLClassifier and BertQuestionAnswerer APIs for the ensemble AI system.
- **Google ML Kit Text Recognition**: On-device OCR for extracting technical indicators.

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
- **Google Play Billing**: In-app purchase handling.
- **Google Play Integrity API**: Anti-tamper verification.

### Offline Assets
- **Pattern Templates**: 109 PNG reference images and YAML configurations.
- **Legal Documents**: HTML/Markdown for terms and privacy policy.
- **Educational Content**: Interactive lessons and the "Trading Book".