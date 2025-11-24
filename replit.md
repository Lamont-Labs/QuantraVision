# QuantraVision

## Overview

QuantraVision is an offline-first Android application designed for retail traders. It provides AI-powered, on-device chart pattern recognition, offering real-time pattern detection, predictive analysis, multi-modal alerts, and explainable AI. The application prioritizes user privacy through exclusive on-device processing and operates without subscriptions or cloud dependencies, offering lifetime access via a one-time payment. Its ambition is to deliver institutional-grade trading intelligence in a privacy-first mobile application.

## User Preferences

Preferred communication style: Simple, everyday language.

Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

QuantraVision is an offline-first Android application built with Jetpack Compose and Material 3 Design System, featuring a dark theme and a chrome/steel metallic brand identity. The UI includes a 5-tab bottom navigation.

**Core Intelligence System:**
The system initially used OpenCV for template matching with 109 PNG reference images, coordinated by `HybridDetectorBridge` and scored by `BayesianFusionEngine`. Multi-signal analysis was performed by combining pattern detection with OCR-extracted indicators (RSI, MACD, volume) using Google ML Kit Text Recognition, with `IndicatorExtractor`, `ContextAnalyzer`, and `QuantraScorer`. A `SmartFilter` handled quality thresholding, and `HistoricalAnalyzer` provided self-improvement. An Ensemble AI Engine facilitated retrieval-based Q&A via `QuantraBot AI Assistant` using all-MiniLM-L6-v2 sentence embeddings, and an AI Explanation Engine generated natural language explanations.

**Apex-Inspired Intelligence System (Current Architectural Shift):**
The architecture is shifting to a sophisticated, multi-layer validation system, replacing simple template matching.
- **Geometric Pattern Detection Engine:** Utilizes OpenCV for geometry-based structural analysis (peaks, troughs, trendlines) to detect core patterns.
- **Trait & Microtrait System:** Categorizes high-level signals into "Traits" and decomposes them into granular "Microtraits" for nuanced analysis and weighted scoring.
- **Mobile Protocol Stack:** A set of 109 deterministic validation rules (Omega01-Omega04 Safety + T01-T80 Tier + LP01-LP25 Learning) adapted from the Apex desktop system. These protocols apply specific logic, modify scores, and ensure fail-closed behavior with an audit trail. The execution order is strictly Omega → Tier → Learning.
- **Entropy, Suppression & Drift Systems:** Mechanisms for detecting conflicting signals (Entropy), learning from past false positives (Suppression Memory), and adapting to changing market conditions (Drift Detection).
- **Enhanced QuantraScore Methodology:** A sophisticated pipeline calculating a 0-100 score, incorporating base trait scoring, microtrait contributions, and penalties/modifiers from entropy, suppression, drift, and all protocols.
- **Hybrid Explanation System:** A two-tier approach using fast template-based explanations and a small LLM (Gemma 2B or Phi-2) for complex cases.
- **Deterministic Proof Logging:** An audit trail for each scan, logging inputs, detected patterns, traits, microtraits, full protocol trace, scores, explanations, and a cryptographic proof hash for integrity verification.
- **Cloud Narration Pipeline:** For paid tiers, integrates with OpenAI API (`CloudReasoner`) for advanced explanations, with a `QuotaGate` enforcing tier-based limits and rate limiting. `LLMContractValidator` ensures response safety and schema compliance. Local `LocalSummaryGenerator` provides deterministic, template-based explanations as a fallback or for the free tier.
- **Quota Management:** `QuotaGate` enforces tier-based cloud call limits (FREE=0, PRO=10/day, ULTRA=25/day), with daily resets and rate limiting.
- **Auto-Explain Manager:** `AutoExplainManager` orchestrates the logic for triggering smart explanations based on eligibility, global preconditions (e.g., `omega_lock`, suppression, entropy, quota), and confidence levels.

**Data Storage & State Management:**
- **Data Storage:** Room database for encrypted local storage of logs, preferences, and scan learning data.
- **State Management:** Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow).

**Authentication & Licensing:**
- Four-tier lifetime access model using Google Play In-App Billing and Google Play Integrity API.

**Other Features:**
- **Alert System:** Centralized `AlertManager` for multi-modal alerts.
- **Real-Time Overlay System:** Uses MediaProjection API for tap-to-scan functionality, with `OverlayRenderer` for display and `ScanThrottler` for performance management (2-4 FPS).
- **Performance & Power Management:** Adaptive Pipeline with `PowerPolicyApplicator`.

## External Dependencies

-   **OpenCV:** For computer vision tasks and geometric pattern detection.
-   **TensorFlow Lite:** On-device ML inference.
-   **TensorFlow Lite Task Text:** Provides NLClassifier and BertQuestionAnswerer APIs.
-   **Google ML Kit Text Recognition:** For OCR-based indicator extraction.
-   **Google Play Billing:** For in-app purchases.
-   **Google Play Integrity API:** For application security.
-   **Gson:** For JSON parsing.
-   **OpenAI API:** Integrated via `CloudReasoner` for advanced cloud narration in paid tiers.
-   **Gemma 2B or Phi-2:** Small language models considered for the hybrid explanation system.
-   **Offline Assets:** Includes legal documents and educational content.