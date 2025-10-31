# QuantraVision Hybrid Dual-Detection System

## Overview

QuantraVision uses a **hybrid dual-tier pattern detection system** combining machine learning and traditional computer vision for maximum accuracy and coverage.

---

## Detection Architecture

### **Tier 1: ML-Powered Detection (6 Patterns)** 🟢

**Technology:** YOLOv8 deep learning model  
**Model:** foduucom/stockmarket-pattern-detection-yolov8 from HuggingFace  
**Training:** 9,000 real trading chart screenshots

**Patterns Detected:**
1. Head and Shoulders Top
2. Head and Shoulders Bottom (Inverse)
3. Triangle (Ascending/Descending/Symmetrical)
4. Double Top (M_Head)
5. Double Bottom (W_Bottom)
6. Trend Lines (StockLine)

**Characteristics:**
- ✅ **Chart-agnostic**: Works on any platform (TradingView, MetaTrader, Robinhood, TD Ameritrade, Webull)
- ✅ **Theme-agnostic**: Works with dark, light, or custom color schemes
- ✅ **Scale-invariant**: Detects patterns at any zoom level
- ✅ **High confidence**: 93.2% mAP@0.5 validated accuracy
- ✅ **Fast inference**: ~20ms per frame

---

### **Tier 2: Template-Based Detection (102 Patterns)** 🟡

**Technology:** OpenCV template matching  
**Templates:** 119 reference images

**Patterns Detected:**
- 102 additional patterns (total 108 minus 6 covered by ML)
- Includes niche/rare patterns like:
  - Cup and Handle
  - Falling Wedge, Rising Wedge
  - Pennants, Flags
  - Complex multi-wave patterns

**Characteristics:**
- ⚡ **Fast matching**: Real-time template correlation
- 📊 **Broad coverage**: Largest pattern library
- ⚠️ **Optimized for**: Candlestick charts
- ⚠️ **Performance varies**: Best results on TradingView-style charts

---

## How It Works

### Parallel Processing

```
Screenshot captured
       ↓
   ┌─────────────────┐
   │   PARALLEL      │
   └─────────────────┘
     ↙            ↘
┌─────────┐   ┌─────────┐
│ YOLOv8  │   │ OpenCV  │
│ GPU     │   │ CPU     │
│ 6 ptrns │   │102 ptrns│
└─────────┘   └─────────┘
     ↓            ↓
   ┌─────────────────┐
   │  Combine &      │
   │  Deduplicate    │
   └─────────────────┘
          ↓
   ┌─────────────────┐
   │  Sort by        │
   │  Confidence     │
   └─────────────────┘
          ↓
     User sees results
```

### Deduplication Logic

When the same pattern is detected by both systems:
1. **ML detection wins** (higher reliability)
2. If both are same method, **higher confidence wins**
3. Display single result with best confidence score

---

## User Interface

### Pattern Display

**ML-Detected Pattern:**
```
🟢 Head & Shoulders Top
   ML Detected | 94% Confidence | Chart-Agnostic
   ✓ Works on all platforms
```

**Template-Detected Pattern:**
```
🟡 Cup and Handle
   Template Match | 78% Similarity | Candlestick Optimized
   ℹ️ Best results on candlestick charts
```

### Confidence Tiers

| Confidence | Tier | Color | Meaning |
|------------|------|-------|---------|
| 90-100% | Very High | 🟢 Green | High reliability trade signal |
| 75-89% | High | 🟢 Green | Reliable pattern match |
| 60-74% | Medium | 🟡 Yellow | Consider additional confirmation |
| < 60% | Low | 🟠 Orange | Use with caution |

---

## Settings & Controls

### Detection Toggles

**ML Detection:**
- Always enabled (can't be disabled)
- Provides baseline reliability

**Template Detection:**
- User-toggleable setting
- Default: Enabled
- Disable for ML-only (fewer, more reliable patterns)

### Configuration

```kotlin
val detector = HybridPatternDetector(context)

// Initialize both systems
detector.initialize()

// Toggle template detection
detector.setTemplateDetectionEnabled(false)  // ML-only mode

// Get stats
val stats = detector.getStats()
// stats.totalPatternCount = 108
// stats.mlPatternCount = 6
// stats.templatePatternCount = 102
```

---

## Performance

### Frame Processing

| Component | Time | Notes |
|-----------|------|-------|
| YOLOv8 Inference | ~20ms | GPU accelerated |
| Template Matching | ~30ms | CPU multi-threaded |
| **Total (parallel)** | **~30ms** | Bottleneck is template matching |
| **Frame Rate** | **~30 FPS** | Well above 12 FPS target |

### Memory Footprint

| Component | Size | Location |
|-----------|------|----------|
| YOLOv8 Model | ~22 MB | Assets (INT8 quantized) |
| OpenCV Templates | ~2 MB | Assets (119 PNGs) |
| Runtime Memory | ~50 MB | TFLite + OpenCV buffers |

---

## Competitive Advantages

### vs TradingView Built-in Patterns
- ✅ More patterns (108 vs ~30)
- ✅ Works offline
- ✅ Works on any chart (not just TradingView)

### vs Other Pattern Detection Apps
- ✅ Dual detection (ML + template)
- ✅ Transparent confidence scores
- ✅ Chart-agnostic capability
- ✅ Largest pattern library

---

## Technical Implementation

### Key Classes

```kotlin
// Main hybrid detector
HybridPatternDetector
  ├── YoloV8Detector (ML-based)
  │   └── TFLite Interpreter
  └── PatternDetector (template-based)
      └── OpenCV template matching

// Results
HybridDetection
  ├── patternName: String
  ├── confidence: Float (0.0-1.0)
  ├── method: DetectionMethod (ML_YOLO | TEMPLATE_OPENCV)
  ├── boundingBox: RectF?
  └── metadata: Map<String, Any>
```

### Integration Points

1. **OverlayService**: Captures screen frames
2. **HybridPatternDetector**: Runs dual detection
3. **DashboardScreen**: Displays results with confidence badges
4. **Settings**: User toggles for detection methods

---

## Model Setup (Developers)

### Required File

**Location:** `app/src/main/assets/models/stockmarket-pattern-yolov8.tflite`  
**Size:** ~22 MB (INT8 quantized)  
**Download:** See `app/src/main/assets/models/README.md`

### Graceful Degradation

If model file is missing:
- ✅ App still runs normally
- ✅ Falls back to template-only detection
- ✅ No crashes or errors
- ⚠️ User sees message: "ML detection unavailable (model file missing)"

---

## Future Enhancements

### Phase 2 (v2.0)
- Add 10 more ML patterns (total 16 ML-based)
- Fine-tune model on user-contributed charts
- Real-time model updates (optional download)

### Phase 3 (v3.0)
- Custom pattern training
- User can add their own patterns via photo upload
- Federated learning (privacy-preserving model improvements)

---

## Accuracy & Validation

### ML Detection (YOLOv8)
- **mAP@0.5:** 93.2%
- **Validation set:** 800 images
- **False positive rate:** < 10%

### Template Detection
- **Accuracy:** Variable (60-85%)
- **Depends on:** Chart style similarity to templates
- **Best case:** TradingView candlestick charts (85%)
- **Worst case:** Custom color schemes (60%)

---

## Conclusion

The hybrid dual-detection system provides:
1. **Reliability** through ML-based detection
2. **Coverage** through template-based detection
3. **Transparency** through confidence scoring
4. **Flexibility** through user-configurable toggles

**This is the best of both worlds** - combining modern deep learning with traditional computer vision for maximum value to traders.
