# QuantraVision

## Overview

QuantraVision is an offline-first Android application for retail traders, providing AI-powered, on-device chart pattern recognition. Its core purpose is to deliver real-time pattern detection, predictive analysis, multi-modal alerts, and explainable AI while prioritizing user privacy through exclusive on-device processing. The application is designed to operate without subscriptions or cloud dependencies, offering lifetime access via a one-time payment.

The project's ambition is to offer institutional-grade trading intelligence within a privacy-first mobile application that functions entirely offline, targeting retail traders seeking advanced analytical tools without compromising data privacy.

## User Preferences

Preferred communication style: Simple, everyday language.

Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

The QuantraVision project is designed as an offline-first Android application using Jetpack Compose with a Material 3 Design System for the UI, featuring a dark theme with a chrome/steel metallic brand identity. It incorporates a 5-tab bottom navigation (Home, Markets, Scan, QuantraBot, Settings).

**Current Core Intelligence System:**
- **Pattern Detection Engine:** Uses OpenCV template matching with 109 PNG reference images, coordinated by `HybridDetectorBridge` and scored by `BayesianFusionEngine`.
- **QuantraCore Intelligence System:** Performs multi-signal analysis by combining pattern detection with OCR-extracted indicators using Google ML Kit Text Recognition. Key components include `IndicatorExtractor`, `ContextAnalyzer`, `QuantraScorer`, and `SmartFilter`.
- **Pattern Learning Engine:** `HistoricalAnalyzer` learns from scan history every 50 scans, operating entirely offline.
- **Ensemble AI Engine:** Provides retrieval-based Q&A using all-MiniLM-L6-v2 sentence embeddings, supporting multiple offline knowledge bases.
- **AI Explanation Engine:** Generates natural language explanations for pattern notifications using the Ensemble AI.
- **QuantraBot AI Assistant:** An interactive trading assistant built on Ensemble AI, utilizing a `QAKnowledgeBase`.

**Future Vision: Apex-Inspired Intelligence System:**
This aims to replace the current template matching with a sophisticated, multi-layer validation system.
- **Geometric Pattern Detection Engine:** Will replace pixel-based template matching with geometry-based structural analysis using OpenCV to detect peaks, troughs, and trendlines, targeting 15-20 core patterns.
- **Trait & Microtrait System:** Categorizes high-level signals into "Traits" and decomposes them into granular "Microtraits" for nuanced analysis and weighted scoring.
- **Mobile Protocol Stack:** A set of 80 deterministic validation rules (T01-T80) adapted from the Apex desktop system. Each protocol applies specific logic, modifies scores, and contributes to an audit trail, ensuring fail-closed guarantees.
- **Entropy, Suppression & Drift Systems:** Mechanisms to detect conflicting signals (Entropy), learn from past false positives (Suppression Memory), and adapt to changing market conditions (Drift Detection).
- **Enhanced QuantraScore Methodology:** A sophisticated pipeline for calculating a 0-100 score, incorporating base trait scoring, microtrait contributions, and penalties/modifiers from entropy, suppression, drift, and all protocols.
- **Hybrid Explanation System:** A two-tier approach using fast template-based explanations and a small LLM (Gemma 2B or Phi-2) for complex cases.
- **Deterministic Proof Logging:** An audit trail for each scan, logging inputs, detected patterns, traits, microtraits, full protocol trace, scores, explanations, and a cryptographic proof hash.

**Data Storage & State Management:**
- **Data Storage:** Room database for encrypted local storage of logs, preferences, and scan learning data.
- **State Management:** Utilizes Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow).

**Authentication & Licensing:**
- Implements a four-tier lifetime access model using Google Play In-App Billing and Google Play Integrity API for security.

**Other Key Features:**
- **Alert System:** Centralized `AlertManager` for multi-modal alerts.
- **Real-Time Overlay System:** Uses MediaProjection API for tap-to-scan functionality.
- **Performance & Power Management:** Adaptive Pipeline with `PowerPolicyApplicator`.

## External Dependencies

- **OpenCV:** For computer vision tasks (template matching, geometric detection).
- **TensorFlow Lite:** On-device ML inference.
- **TensorFlow Lite Task Text:** Provides NLClassifier and BertQuestionAnswerer APIs.
- **Google ML Kit Text Recognition:** For OCR-based indicator extraction.
- **Google Play Billing:** For in-app purchases.
- **Google Play Integrity API:** For application security and anti-tamper verification.
- **Gson:** For JSON parsing.
- **Offline Assets:** Includes legal documents and educational content.
- **Gemma 2B or Phi-2 (Future):** Small language models for the hybrid explanation system.