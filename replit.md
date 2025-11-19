# QuantraVision

## Overview
QuantraVision is an offline-first Android application for retail traders, utilizing AI-powered, on-device chart pattern recognition with OpenCV to identify 109 technical analysis patterns. It offers real-time detection, predictive analysis, multi-modal alerts, and explainable AI with audit trails, all while prioritizing user privacy through on-device processing. The app operates without subscriptions or cloud dependencies, providing lifetime access through a one-time payment model. Its "Intelligence Stack" (Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, Proof Capsules) focuses on offline functionality and educational support to enhance the trading experience.

## User Preferences
Preferred communication style: Simple, everyday language.
Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## Recent Changes
- **2025-11-19**: Version 2.2 released with model import fix - file size now passed from Controller to Worker via WorkManager inputData instead of re-querying URI (which failed in worker context due to permission/context issues)
- **2025-11-19**: Fixed MediaPipe API compatibility - corrected GemmaEngine to use proper MediaPipe 0.10.27 API structure: removed incorrect `BaseOptions` usage, removed non-existent `setResultListener()`/`setTemperature()`/`setTopK()`/`setTopP()` calls from LlmInferenceOptions (these are session-level parameters, not available on base options), implemented minimal correct API with only `setModelPath()` and `setMaxTokens()`
- **2025-11-19**: Implemented mobile-only AI model import feature enabling users to import the 529MB Gemma model directly from their phone using Android Storage Access Framework. Features: file picker integration, background WorkManager copy with progress tracking, configuration-change robustness, automatic model detection after import, StateFlow-based reactive updates, and zero-permission requirement. Users download model from HuggingFace → tap "Import Model" in app → select file → AI ready automatically
- **2025-11-19**: Implemented MediaPipe LLM Inference API integration (v0.10.27) for real on-device AI using Gemma 3 1B model (~529MB .task file). GemmaEngine now uses MediaPipe for true AI inference instead of template-based fallbacks, with shared instance pattern for memory efficiency, GPU acceleration support, and thread-safe singleton access. Switched from Gemma 2B (1.5GB) to Gemma 3 1B (529MB) for 3x smaller size and faster performance
- **2025-11-19**: Enabled Android 13+ predictive back gesture support (`android:enableOnBackInvokedCallback="true"`) for modern navigation UX
- **2025-11-19**: Completed DevBot diagnostic export system with JSON serialization, security confirmation dialog, async file I/O, automatic cleanup (keeps last 5 exports), and secure sharing via FileProvider

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with Material 3 Design System, optimized for a dark theme, featuring a declarative and reactive UI. It employs ViewModel for state management, modular screen architecture, responsive layouts, and a consistent chrome/steel metallic brand identity with high-contrast elements and dual-font typography (Orbitron for headers, Space Grotesk for body). The UI is streamlined, integrates trade scenario display on pattern detection overlays for Pro tier users, and uses an immersive mode. Recent UI updates focus on a professional, app-store-quality appearance with a clean, modern, minimalist aesthetic, enhanced readability, and a 5-tab Material Design 3 bottom navigation bar. A unified StaticBrandBackground composable with a deep space blue-black gradient provides visual depth, complementing neon cyan and gold accents for a futuristic metallic/neon aesthetic.

### Technical Implementations
**Pattern Detection Engine**: Utilizes OpenCV template matching for 109 chart patterns, coordinated by a HybridDetectorBridge, with a BayesianFusionEngine for confidence scoring. TensorFlow Lite is planned for future ML enhancements.
**QuantraCore Intelligence System**: A multi-signal analysis engine combining pattern detection with OCR-extracted technical indicators (RSI, MACD, volume, 30+ others) using Google ML Kit Text Recognition to generate a 0-100 QuantraScore. It uses ContextAnalyzer for confluence detection and QuantraScorer for composite quality scores. SmartFilter automatically filters patterns below a quality threshold (default 60/100). All processing is 100% offline.
**Pattern Learning Engine**: A self-improving adaptive system that learns from every scan to enhance pattern detection accuracy. It analyzes historical scan data via HistoricalAnalyzer to discover statistical indicator profiles for each pattern type, applying learned adjustments to the QuantraScore. It learns every 50 scans, captures all visible indicators via universal OCR, and is 100% offline.
**AI Explanation Engine**: An on-device LLM system providing natural language explanations for detected patterns, built on MediaPipe LLM Inference API with Gemma 2B. Features PatternExplainer, PromptBuilder, ExplanationCache, and FallbackExplanations. Integrated into pattern notifications with an "🧠 Explain" action button, supporting educational Q&A and QuantraScore breakdowns. All explanations are 100% offline.
**QuantraBot AI Assistant**: An interactive AI trading assistant built on MediaPipe with Gemma 2B and a comprehensive expert pattern knowledge base (26 patterns). Features QuantraBotEngine for chat/validation/explanations, PatternPromptBuilder for expert prompt engineering, PatternKnowledgeLoader for caching, and a Material 3 chat UI. Accessible via a dedicated "Bot" tab, it supports AI mode and template-based fallback mode, operating 100% offline.
**DevBot Diagnostic AI** (DEBUG builds only): A real-time application health monitoring system with AI-powered error analysis, built on MediaPipe with Gemma 2B LLM. It appears as a conditional 6th tab in DEBUG builds and features a DiagnosticEngine coordinating 5 monitoring systems (LogcatMonitor, CrashAnalyzer, PerformanceMonitor, NetworkMonitor, DatabaseMonitor). DevBotEngine provides conversational error analysis using DiagnosticPromptBuilder and DiagnosticKnowledgeLoader. It includes a Material 3 chat UI with real-time error statistics and a Diagnostic Export System for JSON export of all diagnostic events. All processing is 100% offline.
**Model Import System** (Mobile-Only): Complete in-app workflow for importing AI models without computer/ADB. Features ModelImportController (Storage Access Framework integration), ImportModelWorker (background 529MB file copy with WorkManager), ImportModelDialog (progress UI with percentage/speed/time remaining), and automatic model detection. Controller is ViewModel-scoped for configuration-change survival, uses unique WorkManager jobs to prevent duplicates, and emits StateFlow for reactive UI updates. ViewModels observe ModelManager.modelStateFlow to auto-initialize engines when model becomes available. Zero permissions required (SAF handles access). User workflow: download model from HuggingFace → open app → tap "Import Model" → select file → AI ready instantly.
**Initialization Contract**: GemmaEngine.initialize() and PatternExplainer.initialize() return Result.failure if the model file is missing, enabling validation while fallback explanations remain operational.
**Scan Learning Engine**: (PRO tier only) Learns from chart scans using perceptual image hashing for privacy-preserving, offline learning.
**Data Storage**: Encrypted Room database for local storage of logs, user preferences, achievements, scan learning data, QuantraScores, and indicator context.
**State Management**: Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Four-tier lifetime access model managed via Google Play In-App Billing, secured with Google Play Integrity API.
**Alert System**: Centralized AlertManager for voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for a tap-to-scan overlay with notification-based results. A FloatingLogoButton triggers single-frame scans, with results appearing in expandable Android notifications. Full touch-passthrough is enabled for everything except the logo button. Android 14 compatibility is ensured by reusing VirtualDisplay and ImageReader. Permission handling checks for OverlayService before requesting MediaProjection permission.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, including logging, digital signing of pattern catalogs, and SBOM maintenance.

### Feature Specifications
- **Intelligence Stack**: Comprises Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules for advanced offline analysis.
- **Pattern-to-Plan Overlay**: Integrates trade scenario display (entry, stop, target) directly into the overlay for instant user access (Pro tier).
- **Offline Functionality**: All core features operate entirely on-device without cloud dependencies.
- **Bottom Navigation Structure**: Features a 5-tab Material 3 NavigationBar (6 tabs in DEBUG builds) with Home, Markets, Scan, QuantraBot (AI Assistant), DevBot (DEBUG only), and Settings sections.
- **Tier Onboarding System**: Post-purchase celebration modal showcasing unlocked features with deep-links, contextual discovery banners, reactive state management, and session-persistent dismissal tracking.

## External Dependencies

### Core ML/CV Libraries
- **OpenCV**: Computer vision for template matching and image processing.
- **MediaPipe LLM Inference API**: On-device AI inference engine for Gemma 2B model with GPU acceleration support.
- **TensorFlow Lite**: Infrastructure for future ML enhancements.
- **Google ML Kit Text Recognition**: On-device OCR for extracting technical indicators from chart screenshots.

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