# QuantraVision Apex

## Overview
QuantraVision Apex is an offline-first Android application designed for retail traders. It provides AI-powered, on-device chart pattern recognition with real-time detection, predictive analysis, and explainable AI. The application operates on privacy-first principles and offers institutional-grade trading intelligence through various monthly subscription tiers (FREE, BASIC, PRO, APEX), with optional cloud-enhanced narration for paid subscribers.

## User Preferences
Preferred communication style: Simple, everyday language.

Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture
The application is an Android native app built with Jetpack Compose and adheres to the Material 3 Design System, featuring a dark theme with a chrome/steel metallic brand identity.

**Key Systems & Features:**
-   **Apex Intelligence Engine:** Employs geometric pattern detection with 109 validation protocols (Omega Safety + Tier Rules + Learning), trait/microtrait analysis, entropy detection, and suppression memory for robust pattern recognition.
-   **QuantraScore Pipeline:** A composite scoring system (0-100) that combines pattern confidence, indicator confluence, and adaptive learning adjustments.
-   **Cloud Narration:** Integrates with the OpenAI API for enhanced explanations in paid tiers, featuring quota enforcement, LLM contract validation, and a local template-based fallback.
-   **Multi-Signal Analysis:** Utilizes Google ML Kit for OCR-extracted indicators (RSI, MACD, volume) and contextual analysis to determine signal confluence.
-   **Real-Time Overlay:** Implemented using the MediaProjection API with a throttled frame rate of 2-4 FPS and tap-to-scan functionality.
-   **Data & Authentication:** Uses an encrypted Room database for local storage and manages a four-tier monthly subscription model (FREE, BASIC, PRO, APEX) via Google Play Billing/Integrity API:
    -   **FREE:** 3 scans/day, 1 AI explanation/day.
    -   **BASIC ($4.99/month):** 25 scans/day, 5 AI explanations/day, 5 saves.
    -   **PRO ($14.99/month):** 75 scans/day, 20 AI explanations/day, 20 saves, batch mode.
    -   **APEX ($29.99/month):** 200 scans/day, 60 AI explanations/day, 100 saves, batch mode + advanced features.
-   **UI/UX:** Dark theme with chrome/steel metallic brand identity.

## External Dependencies
-   **OpenCV:** Utilized for computer vision tasks and geometric pattern detection.
-   **TensorFlow Lite:** For on-device machine learning inference.
-   **TensorFlow Lite Task Text:** Provides APIs for natural language processing, including `NLClassifier` and `BertQuestionAnswerer`.
-   **Google ML Kit Text Recognition:** Employed for OCR-based extraction of indicators from charts.
-   **Google Play Billing:** Manages in-app purchases and subscription services.
-   **Google Play Integrity API:** Ensures the security and integrity of the application.
-   **Gson:** Used for efficient JSON parsing.
-   **OpenAI API:** Integrated for advanced cloud narration capabilities in paid subscription tiers.
-   **Offline Assets:** Includes legal documents and educational content.
-   **Open-License Models:** `scripts/fetch-models.sh` fetches Apache-2.0 licensed AI models like COCO SSD MobileNet v1 (quantized) for object detection and MobileSAM v2 for segmentation, with SHA-256 verification.