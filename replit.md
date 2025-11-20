# QuantraVision

## Overview
QuantraVision is an offline-first Android application designed for retail traders. It leverages AI-powered, on-device chart pattern recognition using OpenCV to identify 109 technical analysis patterns. The app provides real-time detection, predictive analysis, multi-modal alerts, and explainable AI with audit trails, all while ensuring user privacy through exclusive on-device processing. It operates without subscriptions or cloud dependencies, offering lifetime access via a one-time payment. The project's ambition is to empower traders with sophisticated, privacy-centric tools for informed decision-making and educational support through its "Intelligence Stack."

## User Preferences
Preferred communication style: Simple, everyday language.
Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

### UI/UX Decisions
The application utilizes Jetpack Compose with Material 3 Design System, optimized for a dark theme with a consistent chrome/steel metallic brand identity. It features high-contrast elements, dual-font typography (Orbitron for headers, Space Grotesk for body), a streamlined professional UI, and a 5-tab Material Design 3 bottom navigation bar. A unified `StaticBrandBackground` provides visual depth with neon cyan and gold accents.

### Technical Implementations
**Pattern Detection Engine**: Employs OpenCV template matching for 109 chart patterns, coordinated by a `HybridDetectorBridge`, with a `BayesianFusionEngine` for confidence scoring.
**QuantraCore Intelligence System**: A multi-signal analysis engine combining pattern detection with OCR-extracted technical indicators (RSI, MACD, volume, 30+ others) using Google ML Kit Text Recognition to generate a 0-100 `QuantraScore`. Includes `ContextAnalyzer` for confluence detection and `SmartFilter` for quality thresholding, all 100% offline.
**Pattern Learning Engine**: A self-improving adaptive system that learns from every scan to enhance pattern detection accuracy, analyzing historical scan data via `HistoricalAnalyzer`. It learns every 50 scans and is 100% offline.
**Ensemble AI Engine**: A retrieval-based Q&A system using all-MiniLM-L6-v2 sentence embeddings (22MB) for semantic search. It is significantly smaller and faster than single large models. The architecture supports multiple knowledge bases while sharing the same embeddings model. All processing is 100% offline.
**AI Explanation Engine**: Provides natural language explanations for detected patterns using the Ensemble AI Engine, integrated into pattern notifications, all 100% offline.
**QuantraBot AI Assistant**: An interactive AI trading assistant built on the Ensemble AI Engine with a `QAKnowledgeBase` (198 pre-written trading Q&A entries), operating 100% offline.
**DevBot Diagnostic AI** (DEBUG builds only): A real-time application health monitoring system with AI-powered error analysis. It uses its own EnsembleEngine instance with a `DiagnosticKnowledgeBase` (234 diagnostic entries across 10 categories), providing conversational diagnostic analysis 100% offline.
**Knowledge Base System**: Features an interface-based architecture supporting `QAKnowledgeBase` for trading Q&A and `DiagnosticKnowledgeBase` for application diagnostics. Both utilize the same `EmbeddingsRetriever` for semantic search.
**Data Storage**: Encrypted Room database for local storage of logs, user preferences, and scan learning data.
**State Management**: Utilizes Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Four-tier lifetime access model managed via Google Play In-App Billing, secured with Google Play Integrity API.
**Alert System**: Centralized `AlertManager` for multi-modal alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for a tap-to-scan overlay with notification-based results.
**Performance & Power Management**: An Adaptive Pipeline with a `PowerPolicyApplicator` adjusts performance based on device conditions.

### Feature Specifications
- **Intelligence Stack**: Comprises `Regime Navigator`, `Pattern-to-Plan Engine`, `Behavioral Guardrails`, and `Proof Capsules` for advanced offline analysis.
- **Pattern-to-Plan Overlay**: Integrates trade scenario display (entry, stop, target) directly into the overlay (Pro tier).
- **Offline Functionality**: All core features operate entirely on-device without cloud dependencies.
- **Bottom Navigation Structure**: Features a 5-tab Material 3 NavigationBar (6 tabs in DEBUG builds) with Home, Markets, Scan, QuantraBot, and Settings.
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