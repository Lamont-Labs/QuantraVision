# QuantraVision

## Overview
QuantraVision is an offline-first Android application designed for retail traders. It leverages AI-powered, on-device chart pattern recognition using advanced OpenCV template matching to identify 109 technical analysis patterns. The app provides real-time detection, predictive analysis, multi-modal alerts, and explainable AI with audit trails. It prioritizes user privacy through on-device processing and operates without subscriptions or cloud dependencies, offering lifetime access via a 4-tier one-time payment model. Key features include an "Intelligence Stack" (Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, Proof Capsules) focused on offline functionality and educational support to enhance the trading experience.

## User Preferences
Preferred communication style: Simple, everyday language.
Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with the Material 3 Design System, optimized for a dark theme, featuring a declarative and reactive UI. It employs ViewModel for state management, a modular screen architecture, responsive layouts, and a consistent brand identity using a chrome/steel metallic design system with high-contrast elements and a dual-font typography (Orbitron for headers, Space Grotesk for body). The UI is streamlined to essential features, integrates trade scenario display directly on pattern detection overlays for Pro tier users, and uses an immersive mode to maximize screen space. Recent UI updates have focused on achieving a professional, app-store-quality appearance with a clean, modern, minimalist aesthetic, removing excessive borders, blur, and busy backgrounds, while enhancing text readability and component spacing. The navigation architecture has been transformed to a 5-tab Material Design 3 bottom navigation bar for improved discoverability and user experience.

**Background Design System**: All screens utilize a unified StaticBrandBackground composable with a deep space blue-black gradient (top: #010409, center: #0D1825, bottom: #010409) that provides visual depth and makes transparent logo assets visible. This creates a branded, professional visual experience across the entire app with no animations or visual noise. The gradient background complements the neon cyan (#00F0FF) and gold (#FFB347) accent colors, creating a futuristic metallic/neon aesthetic. Content is centered by default for optimal logo visibility, with opt-in alignment control.

### Technical Implementations
**Pattern Detection Engine**: Utilizes an OpenCV template matching system for 109 chart patterns, coordinated by a HybridDetectorBridge, with a BayesianFusionEngine for confidence scoring. TensorFlow Lite infrastructure is planned for future ML enhancements.
**QuantraCore Intelligence System**: Multi-signal analysis engine that combines pattern detection with OCR-extracted technical indicators (RSI, MACD, volume, and 30+ other indicators like ATR, ADX, Stochastic, etc.) to generate a 0-100 QuantraScore for each pattern. Uses Google ML Kit Text Recognition for privacy-preserving on-device indicator extraction, ContextAnalyzer for confluence detection (bullish/bearish/neutral), and QuantraScorer to calculate composite quality scores. SmartFilter automatically filters patterns below quality threshold (default 60/100), eliminating noise and showing only high-probability trade setups. All processing is 100% offline with no cloud dependencies. Scores and indicator context stored in database for historical analysis.
**Pattern Learning Engine**: Self-improving adaptive system that learns from every scan to continuously enhance pattern detection accuracy. Analyzes historical scan data (using HistoricalAnalyzer) to discover statistical indicator profiles for each pattern type. Builds learning profiles through 4 phases: Baseline (<20 scans), Learning (20-99), Adaptive (100-499), Expert (500+). Automatically applies learned adjustments to QuantraScore (+/-20 points) based on how well current indicators match learned typical ranges for each pattern. Re-learns every 50 scans to stay current. Captures ALL visible indicators (not just predefined ones) via universal OCR extraction, storing raw text and indicator values for future pattern discovery. 100% offline, privacy-preserving, requires zero user input.
**AI Explanation Engine**: On-device LLM system providing natural language explanations for detected patterns. Built on TensorFlow Lite with Gemma 2B (Apache 2.0 licensed) foundation. Features PatternExplainer for generating contextual insights, PromptBuilder for intelligent prompt engineering, ExplanationCache for performance optimization (LRU + SharedPreferences), and FallbackExplanations for graceful degradation when model unavailable. Integrated into pattern notifications with "🧠 Explain" action button. Supports educational Q&A, weekly scan summaries, and QuantraScore breakdowns. Model download managed by ModelManager (1.5GB, WiFi-recommended). All explanations 100% offline, privacy-preserving. Future enhancement path ready for actual Gemma TFLite model integration or MediaPipe LLM Inference API.
**Scan Learning Engine**: (PRO tier only) Learns from chart scans using perceptual image hashing for privacy-preserving, offline learning.
**Data Storage**: Encrypted Room database for local storage of logs, user preferences, achievements, scan learning data, QuantraScores, and indicator context.
**State Management**: Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Four-tier lifetime access model managed via Google Play In-App Billing, secured with Google Play Integrity API, signature verification, and R8/ProGuard obfuscation.
**Alert System**: Centralized AlertManager for voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for tap-to-scan overlay with notification-based results. FloatingLogoButton (positioned bottom-right with BOTTOM|END gravity) is the only overlay element. Tap Q logo to trigger single-frame scans. Pattern detection results appear in expandable Android notifications (not on-screen overlays) to eliminate touch-blocking issues. Notifications show pattern list with confidence scores. Long-press Q logo returns to main QuantraVision app. Full touch-passthrough for everything except the logo button itself. **Android 14 Compatibility**: VirtualDisplay and ImageReader are created ONCE during MediaProjection initialization and reused for all scans to comply with Android 14's requirement that `createVirtualDisplay()` can only be called once per MediaProjection instance. **Permission Handling**: ScanViewModel checks if OverlayService is already running before requesting MediaProjection permission again, preventing redundant permission dialogs. Users are guided to select "Share entire screen" via pre-permission dialog (Android system dialog cannot be customized to remove "Share one app" option). **Debug Logging**: Extensive emoji-tagged logs throughout touch handling and scan flow for troubleshooting.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, including logging, digital signing of pattern catalogs, and SBOM maintenance.

### Feature Specifications
- **Intelligence Stack**: Comprises Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules for advanced offline analysis.
- **Pattern-to-Plan Overlay**: Integrates trade scenario display (entry, stop, target) directly into the overlay for instant user access (Pro tier).
- **Offline Functionality**: All core features, including AI pattern recognition and Intelligence Stack, operate entirely on-device without cloud dependencies.
- **Bottom Navigation Structure**: Features a 5-tab Material 3 NavigationBar with Home, Markets, Scan, Learn, and Settings sections, each offering specific functionalities and information.
- **Tier Onboarding System**: Post-purchase celebration modal showcasing unlocked features with deep-links, contextual discovery banners on relevant screens highlighting new capabilities, reactive state management using FeatureDiscoveryStore with CompletableDeferred initialization latch to prevent deadlocks, and session-persistent dismissal tracking via SharedPreferences.

## External Dependencies

### Core ML/CV Libraries
- **OpenCV**: Computer vision for template matching and image processing.
- **TensorFlow Lite**: Infrastructure for future ML enhancements.
- **Google ML Kit Text Recognition**: On-device OCR for extracting technical indicators (RSI, MACD, volume) from chart screenshots. Privacy-preserving and offline.

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