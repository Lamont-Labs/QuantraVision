# YOLOv8 Stock Pattern Detection Model

## Model File Required

**File:** `stockmarket-pattern-yolov8.tflite`  
**Size:** ~88 MB  
**Source:** HuggingFace foduucom/stockmarket-pattern-detection-yolov8

## Download Instructions

### Step 1: Download PyTorch Model
```bash
wget https://huggingface.co/foduucom/stockmarket-pattern-detection-yolov8/resolve/main/model.pt
```

### Step 2: Convert to TensorFlow Lite
```bash
pip install ultralytics

# Convert PyTorch model to TFLite format
yolo export model=model.pt format=tflite int8
```

This creates `model_saved_model/model.tflite`

### Step 3: Add to Project
```
1. Rename: model.tflite → stockmarket-pattern-yolov8.tflite
2. Place in: app/src/main/assets/models/
3. Rebuild project
```

---

## Model Details

**Patterns Detected (6):**
- Head and Shoulders Top
- Head and Shoulders Bottom (Inverse)
- Triangle (Ascending/Descending/Symmetrical)
- Double Top (M_Head)
- Double Bottom (W_Bottom)
- Trend Lines (StockLine)

**Performance:**
- mAP@0.5: 93.2%
- Inference time: ~20ms per frame
- Input size: 640x640 RGB
- Output: Bounding boxes + class probabilities

**Training Data:**
- 9,000 training images
- 800 validation images
- Real trading chart screenshots
- Multiple platforms (TradingView, MetaTrader, etc.)

---

## Why TFLite?

YOLOv8 PyTorch models (`.pt`) cannot run directly on Android. We must convert to TensorFlow Lite (`.tflite`) for on-device inference:

1. **Smaller size**: INT8 quantization reduces from 88MB to ~22MB
2. **Faster inference**: Optimized for mobile GPUs
3. **Android native**: Works with TFLite interpreter already in app

---

## Alternative: Download Pre-converted TFLite

If conversion fails, you can use the Ultralytics export API:
```python
from ultralytics import YOLO
model = YOLO('foduucom/stockmarket-pattern-detection-yolov8')
model.export(format='tflite', imgsz=640, int8=True)
```

---

## Integration Status

✅ Code integration complete  
✅ ML detection pipeline ready  
✅ PyTorch model downloaded (84MB - `stockmarket-pattern-yolov8.pt`)  
⏳ TFLite conversion required (run locally)

---

## ⚠️ IMPORTANT: PyTorch → TFLite Conversion Required

The downloaded `stockmarket-pattern-yolov8.pt` (84MB) is in PyTorch format and **must be converted to TFLite** for Android compatibility.

**Conversion Steps (Run on your local machine):**

```bash
# 1. Install ultralytics
pip install ultralytics

# 2. Convert to TFLite with INT8 quantization
cd app/src/main/assets/models/
yolo export model=stockmarket-pattern-yolov8.pt format=tflite int8 imgsz=640

# This creates: stockmarket-pattern-yolov8_saved_model/stockmarket-pattern-yolov8_full_integer_quant.tflite

# 3. Rename and replace
mv stockmarket-pattern-yolov8_saved_model/*_full_integer_quant.tflite stockmarket-pattern-yolov8.tflite
rm -rf stockmarket-pattern-yolov8_saved_model
rm stockmarket-pattern-yolov8.pt  # Remove PyTorch file after conversion
```

**Alternative (Python script):**
```python
from ultralytics import YOLO

# Load PyTorch model
model = YOLO('stockmarket-pattern-yolov8.pt')

# Export to TFLite (INT8 quantized)
model.export(format='tflite', imgsz=640, int8=True)
```

**Result:** Creates `stockmarket-pattern-yolov8.tflite` (~22MB after quantization)

---

## Why Conversion Failed in Replit

The automated conversion failed due to disk quota limits when installing TensorFlow and PyTorch dependencies (requires ~2-3GB). 

**Solution:** Convert on your local machine where there are no quota restrictions.

---

## For Developers

The app will automatically detect if the model file is present:
- **File exists**: Runs ML-based detection for 6 premium patterns
- **File missing**: Falls back to template-based detection only

No crashes if model is missing - graceful degradation built-in.
