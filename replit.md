# QuantraVision

## Overview

QuantraVision is an offline-first Android application for retail traders featuring AI-powered, on-device chart pattern recognition with real-time detection, predictive analysis, and explainable AI. Built on privacy-first principles with lifetime access via one-time payment, it delivers institutional-grade trading intelligence without subscriptions or cloud dependencies.

## User Preferences

Preferred communication style: Simple, everyday language.

Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

Android application built with Jetpack Compose, Material 3 Design System, dark theme with chrome/steel metallic brand identity. Features sophisticated multi-layer validation system with deterministic pattern detection.

**Key Systems:**
- **Apex Intelligence Engine:** Geometric pattern detection with 109 validation protocols (Omega Safety + Tier Rules + Learning), trait/microtrait analysis, entropy detection, suppression memory
- **QuantraScore Pipeline:** 0-100 composite scoring combining pattern confidence, indicator confluence, adaptive learning adjustments
- **Cloud Narration:** OpenAI API integration for paid tiers (PRO/ULTRA) with quota enforcement, LLM contract validation, local template-based fallback
- **Multi-Signal Analysis:** OCR-extracted indicators (RSI, MACD, volume) via Google ML Kit, context analysis for signal confluence
- **Real-Time Overlay:** MediaProjection API with 2-4 FPS throttling, tap-to-scan functionality
- **Data & Auth:** Room database (encrypted local storage), four-tier lifetime access via Google Play Billing/Integrity API

## External Dependencies

**Current Production Dependencies:**
-   **OpenCV:** For computer vision tasks and geometric pattern detection.
-   **TensorFlow Lite:** On-device ML inference.
-   **TensorFlow Lite Task Text:** Provides NLClassifier and BertQuestionAnswerer APIs.
-   **Google ML Kit Text Recognition:** For OCR-based indicator extraction.
-   **Google Play Billing:** For in-app purchases.
-   **Google Play Integrity API:** For application security.
-   **Gson:** For JSON parsing.
-   **OpenAI API:** Integrated via `CloudReasoner` for advanced cloud narration in paid tiers.
-   **Offline Assets:** Includes legal documents and educational content.

**Future On-Device AI Models (Apache-2.0 Licensed):**
-   **COCO SSD MobileNet v1 (Quantized):** Object detection for chart element recognition (4-6 MB TFLite)
-   **MobileSAM v2:** Segmentation for precise chart region masking (40-50 MB, PyTorch weights)
-   **Model Fetcher:** `scripts/fetch-models.sh` downloads permissive-licensed models with SHA-256 verification
-   **Excluded:** AGPL models (YOLOv5/v8, FastSAM) due to copyleft restrictions incompatible with commercial distribution

## Recent Changes

**November 24, 2025 - Repository Professionalism Update: Acquisition-Grade Quality**

**Documentation Professionalism (6 new/updated files):**
1. **README.md** - Repositioned as production-ready showcase with executive summary, capability matrix, technology badges
2. **SECURITY.md** - Comprehensive security policy with vulnerability reporting, incident response, GDPR/CCPA compliance
3. **ARCHITECTURE.md** - Refreshed to reflect Batch 10 completion (109 protocols, cloud pipeline, quota system, testing strategy)
4. **RELEASE_PLAYBOOK.md** - Complete deployment guide with signing configuration, ProGuard rules, Google Play procedures
5. **docs/README.md** - Central documentation hub with navigation by audience (users, developers, investors, auditors)
6. **docs/DEVELOPMENT_HISTORY.md** - Preserved detailed batch development history

**Code Quality Cleanup:**
- Fixed 14 "QuantraCore" → "QuantraVision" branding references
- Added Apache-2.0 license headers to 7 key public API files (QuotaGate, CloudReasoner, LLMContractValidator, LocalSummaryGenerator, AutoExplainManager, ApexEngineMobile, ScanThrottler)
- Cleaned up TODO/placeholder comments with professional future enhancement notes

**Build & Release:**
- Enhanced CI/CD with Dependabot security scanning references
- Added comprehensive ProGuard configuration template in RELEASE_PLAYBOOK.md
- Streamlined replit.md to 120 lines operational essentials

**Architect Approval:** ✅ PASS - Acquisition-grade professionalism achieved

---

**November 24, 2025 - Post-Batch 10: Open-License Model Fetcher**

**New Infrastructure:**
1. **scripts/fetch-models.sh** - Automated model downloader
   - Downloads Apache-2.0 licensed AI models (COCO SSD MobileNet v1, MobileSAM v2)
   - SHA-256 integrity verification (fail-closed)
   - Excludes AGPL models (YOLOv5/v8, FastSAM)
   - Generates MODEL_MANIFEST.json with checksums

2. **docs/MODEL_FETCHER_GUIDE.md** - Comprehensive documentation
   - Model descriptions and use cases
   - License compliance details
   - Runtime loading examples
   - Troubleshooting guide

3. **.gitignore** - Model file exclusions
   - Large binary model files (.tflite, .pt, .onnx) ignored
   - MODEL_MANIFEST.json kept for integrity verification

**Purpose:**
- Prepares infrastructure for future Apex Intelligence System (Batch 11+)
- Enables geometric pattern detection with on-device vision models
- Maintains strict open-source licensing compliance (Apache-2.0 only)
- Provides fail-closed SHA-256 verification for model integrity

**Usage:**
```bash
# Download permissive-licensed models
bash scripts/fetch-models.sh

# Models installed to app/src/main/assets/models/
# - detector_ssd_mobilenet_v1_quant.tflite (4-6 MB)
# - mobile_sam_v2.pt (40-50 MB)
# - MODEL_MANIFEST.json (metadata + checksums)
```

---

**November 24, 2025 - Batch 10 Complete: Production Hardening & Green CI**

**Test Suites (120+ tests):**
- QuotaGateTest.kt (40+ tests): Tier limits, rate limiting, daily reset, state persistence
- LLMContractValidatorTest.kt (35+ tests): Forbidden words, schema validation, token limits
- LocalSummaryGeneratorTest.kt (25+ tests): Golden tests for all 5 Apex statuses
- VerdictMappingTest.kt (20+ tests): Protocol execution order verification

**CI/CD Infrastructure:**
- GitHub Actions workflow (.github/workflows/ci.yml): lint + tests + assembleDebug
- Strict quality gates (lint failures fail the build)
- APK artifact upload, Gradle caching
- TestFixtures.kt with mock bitmap generators

**Crash Hardening:**
- LiveOverlayController.kt: Enhanced error handling, recovery methods
- SingleFrameCapture.kt: OutOfMemoryError handling, extensive validation
- OverlayService.kt: MediaProjection cleanup, DeadObjectException handling

**Performance Guardrails:**
- ScanThrottler.kt: 2-4 FPS enforcement (333ms target interval)
- Frame rate statistics logging
- User-friendly throttle notifications

**Documentation:**
- docs/verify_demo.md: Complete verification guide
- README.md: Quickstart section

**Architect Approval:** ✅ PASS - Acquisition-grade readiness achieved

---

*For detailed historical development records, see `docs/DEVELOPMENT_HISTORY.md`.*
