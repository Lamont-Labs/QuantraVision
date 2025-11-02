# Pattern Detection Architecture

## Current Implementation: OpenCV Template Matching

QuantraVision uses **100% Apache 2.0 licensed technology** for pattern detection:

### Detection Engine
- **OpenCV 4.10.0** (Apache 2.0 License)
- **Template Matching Algorithm**: Multi-scale normalized cross-correlation
- **Patterns Detected**: 109 distinct technical analysis patterns
- **Performance**: ~15-30ms per frame on modern Android devices

### Pattern Library
All pattern templates are stored as PNG reference images with YAML configuration files in:
- `app/src/main/assets/templates/` (119 template variations)

### Legally Compliant Stack
✅ **OpenCV**: Apache 2.0  
✅ **TensorFlow Lite**: Apache 2.0 (used for future enhancements)  
✅ **ML Kit**: Apache 2.0 (used for OCR)  
✅ **All Dependencies**: Commercially compatible licenses

---

## Architecture Overview

**Production Detection Pipeline:**
```
Chart Screenshot 
  → OpenCV Template Matching (109 patterns)
  → Confidence Scoring
  → Multi-frame Temporal Stabilization
  → Pattern Output
```

**Key Files:**
- `PatternDetector.kt` - Core OpenCV detection engine
- `HybridPatternDetector.kt` - Wrapper with optimization layers
- `HybridDetectorBridge.kt` - Production integration with power management

---

## No External Models Required

Unlike other pattern detection apps, QuantraVision:
- ✅ Works 100% offline (no cloud API calls)
- ✅ Requires no model downloads or conversions
- ✅ Uses only Apache 2.0 licensed technology
- ✅ All processing happens on-device with OpenCV

---

## Future Enhancements

The codebase includes infrastructure for future ML enhancements (all optional):
- `OptimizedHybridDetector.kt` - Ready for custom ML models
- `BayesianFusionEngine.kt` - Multi-source detection fusion
- `TensorPool.kt` - Memory-efficient tensor management

Any future ML models will use Apache 2.0 or MIT licensed alternatives (YOLOX, PP-YOLO, YOLO-NAS).
