# QuantraVision Apex

## Overview
QuantraVision Apex is an Android AI chart-overlay copilot powered by QuantraCore Apex deterministic logic. It provides on-device chart understanding, Apex scoring (0-100), overlay rendering, and optional quota-gated cloud narration.

**Core Promise**: Local vision detects chart structure → Apex logic scores it → overlays highlight it → (paid only) cloud narration explains it.

## What It Is
- On-device open-license vision → structure primitives
- Apex deterministic scoring (0-100)
- Overlay rendering (local only)
- Optional quota-gated cloud narration (text only)
- Full fail-closed behavior

## What It Is NOT
- Not a trading bot
- Not an alerts/signals service
- Not a 109-pattern engine
- Not cloud-vision

## User Preferences
Preferred communication style: Simple, everyday language.

**Always Follow These Steps:**
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling
5. Treat the Master Spec v2.0 as constitution - do not recreate old UI or guess features

## Pipeline (Fixed - Must Not Change)
```
Screen Capture
 → On-device Vision Models
 → Primitive Extractor
 → Apex Engine (T01→T40, LP→Ω)
 → QuantraScore 0–100
 → Overlay Renderer
 → Quota Gate
 → Optional Cloud Narration (text primitives only)
 → LLM Validator (fail-closed)
```

## Subscription Tiers (Hard-Capped)
| Tier | Price | Scans/Day | Narrations/Day | Batch Mode |
|------|-------|-----------|----------------|------------|
| FREE | $0 | 3 | 1 | OFF |
| STARTER/BASIC | $4.99 | 25 | 5 | OFF |
| PRO | $14.99 | 75 | 20 | ON |
| APEX | $29.99 | 200 | 60 | ON |

**Note**: Scans are tier-limited. Overlays + Apex logic run even at quota 0, but UI blocks new scans. Cloud narration is separately quota-gated.

## Cloud Rules
- Never upload screenshots/images
- Cloud sees primitives only (score, trend, strength, pressure, risk, levels, trendlines)
- Cloud cannot override Apex verdict
- Narration is tier-limited
- All cloud output validated for financial advice (blocked if detected)

## Model License Rules
- Only MIT/BSD/Apache models allowed
- No AGPL/unknown license models
- If license unclear → block

**FORBIDDEN (Acquisition Risk):**
- Ultralytics YOLOv5/YOLOv8 weights (AGPL)
- FastSAM weights (AGPL)

## Current Models (Legacy Placeholders - Pre-v2.0)
These are temporary placeholders until v2.0 model swap:
| Model | File | Size | License | Purpose |
|-------|------|------|---------|---------|
| COCO SSD MobileNet v1 | detector_ssd_mobilenet_v1_quant.tflite | 4 MB | Apache-2.0 | Object detection |
| DeepLabv3 MobileNetV2 | deeplabv3_segmentation.tflite | 2.7 MB | Apache-2.0 | Semantic segmentation |
| Sentence Embeddings | sentence_embeddings.tflite | 22 MB | Apache-2.0 | Text embedding for AI |

## Target Models (v2.0 Spec - Section 8.10)
Per Master Spec v2.0, these placeholder models will be used for build compatibility:
| Model | File | License | Purpose |
|-------|------|---------|---------|
| Canny Edge | canny_edge.tflite | Apache-2.0 | Detect trendlines, wicks, edges |
| HED | hed.tflite | Apache-2.0 | Contour detection for candles |
| MobileNet V3 | mobilenet_v3.tflite | Apache-2.0 | Chart segment classification |
| UNet Small | unet_small.tflite | Apache-2.0 | Support/resistance zone detection |
| Tiny Line | tiny_line.tflite | MIT | Trendline detection |

**Fallback Rule**: If any model fails to download, use no-op stub returning empty primitives. Never fail build due to model issues.

## Apex Engine Protocols
**Tier Protocols (T01-T40)**: Input validation, candle health, noise penalty, trend estimation, volatility, level alignment, breakout/breakdown, compression, momentum, fusion, entropy

**Learning Protocols (LP01-LP05)**: Reinforcement weighting, microtrait calibration, suppression memory, hallucination guard, drift estimator (local only, resets on launch)

**Omega Safety (Ω1-Ω3)**: Cannot be bypassed - fail-closed on any trigger

## Determinism Rules
- Same input → same score → same overlays → same hash
- No randomness, no fluctuations
- All math uses fixed rounding (3 decimals)
- Missing permissions/models/config = fail-closed

## Color System
- apexCyan: #00E0FF
- apexBlue: #005CFF
- apexRed: #FF3B3B
- apexGreen: #00FF8C
- apexYellow: #FFDB4D
- textPrimary: #D7E6FA
- backgroundDeep: #030A14

## Branding
- "Powered by QuantraCore Apex™ logic"
- "Built by Lamont Labs"
- Footer: "Educational use only. Not financial advice."

## File Structure (Target - v2.0 Spec)
```
android/app/src/main/java/com/lamontlabs/quantravision/
  MainActivity.kt
  overlay/      (OverlayRenderer, FloatingToggleService, etc.)
  apex/         (ApexEngine, Traits, Protocols, ScoreAssembler)
  vision/       (VisionManager, PrimitivesBuilder, ModelLoader)
  subscription/ (TierManager, QuotaManager, BillingManager)
  cloud/        (CloudClient, CloudValidator, CloudQuota)
  legal/        (DisclaimerManager, LegalAgreementScreen)
  security/     (ModelIntegrityChecker, KeyStoreHandler)
  safety/       (FailClosedManager, OmegaSafety)
  ui/hud/       (ApexHUD, HUDScoreBlock, HUDVerdictBlock)
  ui/screens/   (SplashScreen, HomeScreen, SettingsScreen, SubscriptionScreen)
```

## Recent Changes (November 2025)
- Replaced MobileSAM PyTorch (39MB) with DeepLabv3 TFLite (2.7MB) - acquisition safe
- Fixed all "Unlimited" quota messaging - now shows hard caps
- Added branding: "Powered by QuantraCore Apex™ logic" and "Built by Lamont Labs"
- Terminology: "Scans" (not "Highlights") throughout UI
- Removed DevBot from navigation
- Created ChartSegmenter.kt wrapper for TFLite segmentation

## Critical Design Rules
- **No unlimited features**: All tiers have hard-capped daily limits
- **Fail-closed architecture**: Pattern detection follows deterministic, conservative approach
- **Privacy-first**: No chart images sent to cloud - only text primitives for narration
- **Deterministic**: Same input = same output = same hash
