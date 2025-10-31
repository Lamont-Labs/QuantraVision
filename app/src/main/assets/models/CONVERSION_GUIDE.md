# YOLOv8 Model Conversion Guide

## Current Status
✅ **PyTorch model ready**: `stockmarket-pattern-yolov8.pt` (84MB)  
⏳ **TFLite conversion needed**: Use one of the methods below

---

## Option 1: Google Colab (Recommended - No Desktop Needed!)

**Perfect for mobile/browser-only access**

### Steps:

1. **Open this Google Colab notebook** (click link):  
   👉 https://colab.research.google.com/

2. **Create a new notebook** and paste this code:

```python
# Install ultralytics
!pip install ultralytics -q

# Upload your PyTorch model
from google.colab import files
print("📤 Upload your stockmarket-pattern-yolov8.pt file:")
uploaded = files.upload()

# Convert to TFLite
from ultralytics import YOLO
import os

model_name = list(uploaded.keys())[0]
print(f"\n🔄 Converting {model_name} to TFLite...")

model = YOLO(model_name)
export_path = model.export(format='tflite', imgsz=640, int8=True)

print(f"✅ Conversion complete!")

# Find and download the TFLite file
import glob
tflite_files = glob.glob("*_saved_model/*_full_integer_quant.tflite")

if tflite_files:
    tflite_path = tflite_files[0]
    print(f"📥 Downloading: {tflite_path}")
    print(f"   Size: {os.path.getsize(tflite_path)/1024/1024:.1f} MB")
    
    # Download to your device
    files.download(tflite_path)
    
    print("\n✅ Done! Rename the downloaded file to:")
    print("   stockmarket-pattern-yolov8.tflite")
    print("\n📁 Then place it in:")
    print("   app/src/main/assets/models/")
else:
    print("❌ TFLite file not found")
```

3. **Run the notebook** (click ▶️ button)
4. **Upload** your `stockmarket-pattern-yolov8.pt` file when prompted
5. **Wait 2-3 minutes** for conversion
6. **Download** the generated `.tflite` file
7. **Upload it back** to your Replit project at `app/src/main/assets/models/`

---

## Option 2: Local Machine (When You Have Desktop Access)

```bash
# Install ultralytics
pip install ultralytics

# Navigate to models folder
cd app/src/main/assets/models/

# Convert to TFLite
yolo export model=stockmarket-pattern-yolov8.pt format=tflite int8 imgsz=640

# Rename output
mv stockmarket-pattern-yolov8_saved_model/*_full_integer_quant.tflite stockmarket-pattern-yolov8.tflite

# Clean up
rm -rf stockmarket-pattern-yolov8_saved_model
rm stockmarket-pattern-yolov8.pt
```

---

## Option 3: Use App Without ML Model (Works Now!)

Your app has **graceful fallback**:
- ❌ **No TFLite**: Uses 108 OpenCV template patterns (already working)
- ✅ **With TFLite**: Adds 6 premium ML-detected patterns (Head&Shoulders, Triangles, etc.)

**The app works perfectly fine without the ML model!**

---

## Expected Results

After conversion, you should have:
- **File**: `stockmarket-pattern-yolov8.tflite`
- **Size**: ~22-25 MB (compressed from 84MB via INT8 quantization)
- **Patterns**: 6 premium chart patterns (mAP@0.5: 93.2%)

---

## Troubleshooting

**Q: Google Colab conversion fails?**  
A: Make sure you're using a free Colab runtime (GPU not required, CPU is fine)

**Q: Downloaded file is huge?**  
A: Make sure `int8=True` is in the export command for quantization

**Q: App crashes with TFLite file?**  
A: Verify file size is ~22MB. If it's wrong size, reconvert with correct settings.

---

## Quick Test

Once you have the `.tflite` file in place:
1. Build your Android app
2. Take a screenshot of a chart with Head & Shoulders pattern
3. Run detection
4. Check logs for "ML model loaded" vs "Using template fallback"
