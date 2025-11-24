# QuantraVision

## Overview

QuantraVision is an offline-first Android application for retail traders, providing AI-powered, on-device chart pattern recognition. Its core purpose is to deliver real-time pattern detection, predictive analysis, multi-modal alerts, and explainable AI while prioritizing user privacy through exclusive on-device processing. The application is designed to operate without subscriptions or cloud dependencies, offering lifetime access via a one-time payment.

The project's ambition is to offer institutional-grade trading intelligence within a privacy-first mobile application that functions entirely offline, targeting retail traders seeking advanced analytical tools without compromising data privacy.

## Project Status

**Current State:** Active Development - Apex Engine Mobile Implementation (November 2025)

**Build Status:**
- ✅ 100+ successful builds on Samsung S23 FE
- ✅ **Batch 8 Complete: All 109 Protocols implemented (T01-T80 + LP01-LP25 + Omega01-Omega04)**
- ✅ Apex Engine Mobile core (ApexEngineMobile, QuantraScoreMobile, ProofHasher) operational
- 🔄 Template matching detection implemented but needs optimization
- 📊 OCR indicator extraction requires refinement
- 🚀 Apex Intelligence System complete (Batches 0-8: Tier + Learning + Omega Safety)

## User Preferences

Preferred communication style: Simple, everyday language.

Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

QuantraVision is an offline-first Android application utilizing Jetpack Compose with a Material 3 Design System, featuring a dark theme and a chrome/steel metallic brand identity. The UI incorporates a 5-tab bottom navigation (Home, Markets, Scan, QuantraBot, Settings).

**Core Intelligence System:**
The current system uses OpenCV for template matching with 109 PNG reference images, coordinated by `HybridDetectorBridge` and scored by `BayesianFusionEngine`. It performs multi-signal analysis by combining pattern detection with OCR-extracted indicators (RSI, MACD, volume) using Google ML Kit Text Recognition, with `IndicatorExtractor`, `ContextAnalyzer`, and `QuantraScorer` as key components. A `SmartFilter` handles quality thresholding. The `HistoricalAnalyzer` provides a self-improving system by learning from scan history. An Ensemble AI Engine provides retrieval-based Q&A using all-MiniLM-L6-v2 sentence embeddings and powers the `QuantraBot AI Assistant`. An AI Explanation Engine generates natural language explanations for pattern notifications.

**Apex-Inspired Intelligence System (Planned Architectural Shift):**
This future vision aims to replace template matching with a sophisticated, multi-layer validation system.
- **Geometric Pattern Detection Engine:** Will use OpenCV for geometry-based structural analysis (peaks, troughs, trendlines) to detect 15-20 core patterns with higher accuracy.
- **Trait & Microtrait System:** Categorizes high-level signals into "Traits" and decomposes them into granular "Microtraits" for nuanced analysis and weighted scoring.
- **Mobile Protocol Stack:** A set of 109 deterministic validation rules (Omega01-Omega04 Safety + T01-T80 Tier + LP01-LP25 Learning) adapted from the Apex desktop system. These protocols apply specific logic, modify scores, and contribute to an audit trail, ensuring fail-closed behavior.
- **Entropy, Suppression & Drift Systems:** Mechanisms to detect conflicting signals (Entropy), learn from past false positives (Suppression Memory), and adapt to changing market conditions (Drift Detection).
- **Enhanced QuantraScore Methodology:** A sophisticated pipeline for calculating a 0-100 score, incorporating base trait scoring, microtrait contributions, and penalties/modifiers from entropy, suppression, drift, and all protocols.
- **Hybrid Explanation System:** A two-tier approach using fast template-based explanations and a small LLM (Gemma 2B or Phi-2) for complex cases.
- **Deterministic Proof Logging:** An audit trail for each scan, logging inputs, detected patterns, traits, microtraits, full protocol trace, scores, explanations, and a cryptographic proof hash for integrity verification.

**Data Storage & State Management:**
- **Data Storage:** Room database for encrypted local storage of logs, preferences, and scan learning data.
- **State Management:** Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow).

**Authentication & Licensing:**
- Four-tier lifetime access model using Google Play In-App Billing and Google Play Integrity API.

**Other Features:**
- **Alert System:** Centralized `AlertManager` for multi-modal alerts.
- **Real-Time Overlay System:** Uses MediaProjection API for tap-to-scan functionality.
- **Performance & Power Management:** Adaptive Pipeline with `PowerPolicyApplicator`.

## External Dependencies

**Current Dependencies:**
- **OpenCV:** For computer vision tasks.
- **TensorFlow Lite:** On-device ML inference.
- **TensorFlow Lite Task Text:** Provides NLClassifier and BertQuestionAnswerer APIs.
- **Google ML Kit Text Recognition:** For OCR-based indicator extraction.
- **Google Play Billing:** For in-app purchases.
- **Google Play Integrity API:** For application security.
- **Gson:** For JSON parsing.
- **Offline Assets:** Includes legal documents and educational content.

**Future Dependencies (if Apex Intelligence is implemented):**
- **Gemma 2B or Phi-2:** Small language models for the hybrid explanation system.
## Recent Changes

**November 24, 2025 - Batch 9 Complete: Cloud Narration Pipeline + On-Device Wiring**

**New Components (7 files):**
1. **QuotaGate.kt** - Tier-based cloud call quota enforcement (FREE=0, PRO=10/day, ULTRA=25/day)
   - Daily reset at midnight with timezone-aware logic
   - Rate limiting: 8s min between calls, max 3 per 60 seconds
   - Persistent JSON state management (quota_state.json)
2. **CloudReasoner.kt** - OpenAI API integration for paid-tier narration
   - 15-second timeout enforcement
   - Tier-based token limits (PRO=180, ULTRA=380)
   - Only sends structured JSON packets (never screenshots)
3. **LLMContractValidator.kt** - Response validation and safety enforcement
   - Forbidden words check (buy, sell, long, short, enter, exit, etc.)
   - JSON schema validation (10 required fields)
   - Status echo verification and token limit enforcement
4. **LocalSummaryGenerator.kt** - Deterministic template-based explanations
   - Universal header (status, QuantraScore, confidence, entropy, regime)
   - Status-specific templates (PASS, WAIT, FAIL, SUPPRESSED, OMEGA)
   - Spec-compliant fallback for FREE tier and cloud failures
5. **AutoExplainManager.kt** - Auto-trigger logic for smart explanations
   - Eligibility: STARTER/STANDARD/PRO tiers (maps to spec PRO/ULTRA)
   - Global preconditions (omega_lock, suppression, entropy, quota)
   - Triggers: WAIT (confidence ≥0.55), PASS (mid-conf + low-entropy + user toggle)
6. **PrimitiveExtractor.kt** - Vision extraction orchestration
   - Deterministic pixel-based SHA-256 hash (no timestamp)
   - Real OCR extraction using IndicatorExtractor
   - Replaces ChartPrimitives.stub() usage
7. **ExplainOrchestrator.kt** - Complete cloud pipeline wiring
   - Tier check → QuotaGate → CloudReasoner → Validator → LocalSummary fallback
   - Quota increments on ALL attempts (success, violation, failure)
   - Fail-closed behavior throughout

**Modified Components (3 files):**
1. **OverlayRenderer.kt** - Auto-dim overlay logic for quota enforcement
   - Dims to 50% opacity when FREE tier or quota exhausted
   - Shows contextual upgrade messages
2. **ApexModels.kt** - Removed ChartPrimitives.stub() method
3. **ProtocolRegistryMobile.kt** - Fixed stub verdict to use real protocol results

**Critical Fixes Applied:**
- ✅ Tier mapping consistency across all 3 files (AutoExplainManager, ExplainOrchestrator, OverlayRenderer)
- ✅ STARTER ($9.99) → PRO quota (10 calls/day)
- ✅ STANDARD ($24.99) → ULTRA quota (25 calls/day)
- ✅ PRO ($49.99) → ULTRA quota (25 calls/day)
- ✅ Deterministic hashes for deduplication (pixel-based, no timestamp)
- ✅ Quota counts ALL cloud attempts (spec line 320 compliance)
- ✅ All paid tiers eligible for auto-explain

**Architecture:**

**On-Device Pipeline:**
```
Bitmap → PrimitiveExtractor.extract()
      → ChartPrimitives (OCR, hash, type)
      → ApexEngineMobile.runScan()
      → ApexResult (109 protocols: Omega→Tier→Learning)
      → OverlayRenderer (auto-dim on quota/tier)
```

**Paid Cloud Pipeline:**
```
ApexResult → AutoExplainManager.shouldAutoExplain()
          → QuotaGate.canMakeCloudCall() [FREE→false]
          → CloudReasoner.narrate() [OpenAI, 15s timeout]
          → QuotaGate.incrementCallCount() [count all attempts]
          → LLMContractValidator.validate()
          → [VALID] Format explanation
          → [INVALID/ERROR] LocalSummaryGenerator.generate()
```

**Architect Approval:** ✅ PASS - "Tier-to-quota mappings now align with the Batch 9 spec, so paid tiers reach cloud narration while FREE remains gated."

---

**November 24, 2025 - Batch 8 Complete: Omega Safety Protocols (Omega01-Omega04)**
- Implemented all 4 Omega Safety Protocols as final hard locks before Tier protocols
- Four critical safety categories:
  - Omega01: Structural Anomaly Guard (weight 5.0) - Validates candle integrity
  - Omega02: Risk Cap Enforcer (weight 4.8) - Enforces risk limits
  - Omega03: Security & Authorization Validator (weight 4.9) - Validates authorization and proof integrity
  - Omega04: Compliance Guard (weight 4.7) - Enforces disclaimers and tier restrictions
- Fixed critical fail-closed violation in Omega02
- **Total: 109 Protocols implemented (T01-T80 + LP01-LP25 + Omega01-Omega04)**
- **Execution order:** Omega → Tier → Learning (strict safety-first)
