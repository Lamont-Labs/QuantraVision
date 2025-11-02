# QuantraVision Validation & Accuracy Testing Guide

## Overview

This guide explains how to test and validate pattern detection accuracy before launching QuantraVision.

---

## Quick Start

### Step 1: Prepare Test Images

1. Take 20 screenshots of charts with known patterns (TradingView recommended)
2. Name files: `pattern-name_001.png`
   - Examples: `head-and-shoulders-top_001.png`, `double-top_002.png`
3. Place in `validation_images` directory

### Step 2: Run Validation

```kotlin
val runner = SimpleTestRunner(context)
val report = runner.runQuickTest()
println(report.prettyPrint())
```

### Step 3: Interpret Results

| Accuracy | Verdict | Action |
|----------|---------|--------|
| 90-100% | 🟢 Excellent | Production ready |
| 70-89% | 🟡 Good | Minor tuning needed |
| 50-69% | 🟠 Needs work | Improve templates |
| <50% | 🔴 Not ready | Major issues to fix |

---

## Validation Framework Architecture

```
ValidationFramework
├── TestCase (image + expected pattern)
├── ValidationResult (detected patterns + correctness)
└── AccuracyReport (metrics + per-pattern breakdown)

SimpleTestRunner
├── runQuickTest() → Load images, run tests, generate report
├── testSingleImage() → Test one image manually
└── createTestDirectoryTemplate() → Setup validation directory
```

---

## Accuracy Metrics Explained

### Overall Metrics

**Accuracy**
- Definition: % of correct detections
- Formula: (Correct Detections / Total Tests) × 100
- Target: ≥70% for MVP, ≥90% for premium product

**Precision**
- Definition: Of all patterns detected, how many were correct?
- Formula: True Positives / (True Positives + False Positives)
- High precision = Few false alarms

**Recall**
- Definition: Of all actual patterns, how many did we find?
- Formula: True Positives / (True Positives + False Negatives)
- High recall = Few missed patterns

**F1 Score**
- Definition: Balance between precision and recall
- Formula: 2 × (Precision × Recall) / (Precision + Recall)
- Best: 1.0, Worst: 0.0

### Per-Pattern Breakdown

The report shows accuracy for each pattern type:

```
🟢 Head & Shoulders: 95% (19/20)
🟢 Double Top: 85% (17/20)
🟡 Triangle: 75% (15/20)
🔴 Cup & Handle: 45% (9/20)
```

This helps identify which patterns need improvement.

---

## How to Get Test Images

### Method 1: TradingView Screenshots (Recommended)

**Why:** Free, pre-labeled, diverse patterns

**Steps:**
1. Go to https://www.tradingview.com/chart/
2. Add indicator: "All Chart Patterns"
3. TradingView auto-detects patterns (your ground truth)
4. Press Alt+S (Windows) or Cmd+S (Mac) to screenshot
5. Save as `pattern-name_001.png`

**Pro tip:** Use different:
- Assets (BTC, AAPL, EUR/USD)
- Timeframes (5m, 1h, 1d)
- Themes (dark/light)

### Method 2: Download HuggingFace Dataset

**Why:** 9,800 pre-labeled images for extensive testing

**Steps:**
```bash
git clone https://huggingface.co/datasets/foduucom/stockmarket-pattern-detection-yolov8
```

Then select 20-50 images from validation folder.

### Method 3: Stock Photo Sites

Sites like Pexels, Freepik have chart pattern images (check licenses).

---

## Existing Accuracy Features

QuantraVision **already implements** advanced accuracy improvements:

### ✅ Multi-Scale Template Matching

**What:** Tests each template at multiple zoom levels  
**Code:** `ScaleSpace.scales(cfg)` (PatternDetector.kt:59)  
**Impact:** Detects patterns regardless of chart zoom

```kotlin
for (s in ScaleSpace.scales(cfg)) {
    val scaled = ScaleSpace.resizeForScale(input, s)
    // Match template at this scale
}
```

### ✅ Color Normalization

**What:** Converts to grayscale before matching  
**Code:** `Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2GRAY)` (line 49)  
**Impact:** Works on dark theme, light theme, custom colors

### ✅ Confidence Calibration

**What:** Maps raw scores to intuitive confidence levels  
**Code:** `ConfidenceCalibrator.calibrate(patternName, score)` (line 77)  
**Impact:** User-friendly confidence percentages

### ✅ Temporal Smoothing

**What:** Requires pattern detected across multiple frames  
**Code:** `TemporalTracker.update(...)` (line 78)  
**Impact:** Reduces flickering false positives

### ✅ Consensus Engine

**What:** Aggregates detections across scales  
**Code:** `ConsensusEngine.compute(patternName, scaleMatches)` (line 76)  
**Impact:** Only reports patterns with multi-scale agreement

---

## How Detection Works (Technical)

### Pipeline Overview

```
1. Input Image (Bitmap)
   ↓
2. Convert to Grayscale (color normalization)
   ↓
3. Multi-Scale Matching
   ├─ Scale 0.5x → Match template
   ├─ Scale 0.75x → Match template
   ├─ Scale 1.0x → Match template
   ├─ Scale 1.25x → Match template
   └─ Scale 1.5x → Match template
   ↓
4. Consensus Engine (aggregate results)
   ↓
5. Confidence Calibration (map to %)
   ↓
6. Temporal Tracking (smooth over time)
   ↓
7. Return Pattern Match
```

### Code Flow

```kotlin
// PatternDetector.kt - Main detection logic
suspend fun scanStaticAssets() {
    val templates = templateLibrary.loadTemplates()
    
    dir.listFiles()?.forEach { imageFile ->
        val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
        val input = Mat()
        Utils.bitmapToMat(bmp, input)
        
        // Color normalization
        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2GRAY)
        
        templates.groupBy { it.name }.forEach { (patternName, family) ->
            val scaleMatches = mutableListOf<ScaleMatch>()
            
            family.forEach { template ->
                // Multi-scale matching
                for (scale in ScaleSpace.scales(template.config)) {
                    val scaled = ScaleSpace.resizeForScale(input, scale)
                    Imgproc.matchTemplate(scaled, template.image, result, TM_CCOEFF_NORMED)
                    
                    val confidence = Core.minMaxLoc(result).maxVal
                    if (confidence >= template.threshold) {
                        scaleMatches.add(ScaleMatch(patternName, confidence, scale))
                    }
                }
            }
            
            // Consensus + Calibration + Temporal
            val consensus = ConsensusEngine.compute(patternName, scaleMatches)
            val calibrated = ConfidenceCalibrator.calibrate(patternName, consensus.score)
            val temporal = TemporalTracker.update(key, calibrated, timestamp)
            
            // Store result
            db.insert(PatternMatch(
                patternName = patternName,
                confidence = calibrated,
                // ...
            ))
        }
    }
}
```

---

## Interpreting Validation Reports

### Example Report

```
============================================================
VALIDATION REPORT
============================================================

Overall Metrics:
  Total Tests: 20
  Accuracy: 85%
  Precision: 88%
  Recall: 82%
  F1 Score: 85%

Detection Performance:
  ✓ Correct: 17
  ✗ False Positives: 2
  ✗ False Negatives: 3

Timing:
  Avg Processing: 45ms per image

Per-Pattern Accuracy:
  🟢 Head and Shoulders: 95% (19/20)
  🟢 Double Top: 90% (18/20)
  🟡 Triangle: 75% (15/20)
  🟡 Wedge: 70% (14/20)
  🔴 Cup and Handle: 50% (10/20)

============================================================
```

### What Each Metric Means

**Total Tests: 20**
- You tested with 20 images

**Accuracy: 85%**
- 17 out of 20 patterns detected correctly
- Good enough for MVP launch

**Precision: 88%**
- When detector says "pattern found," it's right 88% of the time
- Low false alarm rate

**Recall: 82%**
- Finds 82% of actual patterns present
- Misses 18% (false negatives)

**F1 Score: 85%**
- Balanced performance between precision/recall

**Per-Pattern:**
- Head & Shoulders: Excellent (95%)
- Cup & Handle: Needs improvement (50%)

### Action Items Based on Results

**If Accuracy ≥ 90%:**
- ✅ Ship it! Production ready
- Consider as premium selling point

**If Accuracy 70-89%:**
- ✅ Good enough for v1.0 launch
- Add disclaimer: "Beta - accuracy improving"
- Identify weak patterns and improve templates
- Gather user feedback for v2.0

**If Accuracy 50-69%:**
- ⚠️ Need improvement before launch
- Focus on fixing low-scoring patterns
- May need better templates or ML model
- Consider launching with fewer patterns (high-accuracy ones only)

**If Accuracy < 50%:**
- ❌ Not ready for launch
- Major issues with detection
- Possible causes:
  - Templates optimized for wrong chart style
  - Need chart-agnostic ML approach
  - Test images not representative

---

## Common Issues & Fixes

### Issue: Low accuracy on specific chart platform

**Cause:** Templates optimized for TradingView, testing on MetaTrader

**Fix:**
1. Add templates from target platform
2. Or use ML detection (chart-agnostic)
3. Or focus on one platform initially

### Issue: Dark theme works, light theme fails

**Cause:** Color normalization not applied

**Check:** Line 49 in PatternDetector.kt should have grayscale conversion

**Status:** ✅ Already implemented

### Issue: Detection flickers (pattern appears/disappears)

**Cause:** Temporal smoothing threshold too strict

**Fix:** Adjust TemporalTracker settings

**Status:** ✅ Already implemented

### Issue: False positives on partial patterns

**Cause:** Confidence threshold too low

**Fix:** Adjust template threshold or calibration curve

---

## Next Steps After Validation

### If Accuracy ≥ 70%:

1. **Ship v1.0**
   - Launch with honest disclaimers
   - "Accuracy varies by chart style"
   - "Best results on candlestick charts"

2. **Gather Real-World Data**
   - Add feedback buttons ("Correct" / "Wrong")
   - Track which patterns users find useful
   - Collect anonymized accuracy stats

3. **Iterate in v2.0**
   - Retrain ML model with user feedback
   - Add federated learning
   - Improve low-scoring patterns

### If Accuracy < 70%:

1. **Don't launch yet**
   - Risk of negative reviews
   - "Doesn't work" complaints

2. **Fix Core Issues**
   - Test different template sets
   - Use ML-only mode (6 patterns at 93% accuracy)
   - Partner with beta testers

3. **Pivot if Needed**
   - Education-first (free patterns + paid courses)
   - Niche down (crypto-only, forex-only)
   - B2B (sell to trading platforms)

---

## FAQ

**Q: How many test images do I need?**  
A: Minimum 20, ideal 50-100 for statistical significance.

**Q: Can I use real trading charts?**  
A: Yes! TradingView screenshots are perfect ground truth.

**Q: What if I don't have 109 patterns to test?**  
A: Focus on 6-10 most common patterns first. Validate those, then expand.

**Q: Should I test ML and template separately?**  
A: Yes! ValidationFramework shows which method detected each pattern.

**Q: What accuracy is "good enough"?**  
A: 70% minimum for launch, 80%+ competitive, 90%+ premium product.

**Q: Can validation test real-time overlay?**  
A: No, this tests static images only. Real-time testing is separate.

---

## Summary

**The validation framework lets you:**
- ✅ Measure actual detection accuracy
- ✅ Identify weak patterns
- ✅ Compare ML vs template performance
- ✅ Make data-driven launch decisions

**You already have implemented:**
- ✅ Multi-scale matching
- ✅ Color normalization
- ✅ Confidence calibration
- ✅ Temporal smoothing
- ✅ Consensus engine

**What you need to do:**
1. Get 20 test images (30 minutes)
2. Run validation (5 minutes)
3. Review report (5 minutes)
4. Decide: Ship, improve, or pivot

**Bottom line:** You're 40 minutes away from knowing if your app works! 🚀
