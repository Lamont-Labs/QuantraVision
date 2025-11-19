# Gemma 2B Model Download Instructions

## Overview

QuantraVision uses **Google Gemma 2B Instruct** for on-device AI explanations. This model runs 100% offline on your device using MediaPipe LLM Inference API.

## Model Specifications

- **Model**: Gemma 2B Instruct (INT8 Quantized)
- **Format**: MediaPipe `.task` file (not raw TFLite)
- **Size**: ~1.5GB
- **License**: Apache 2.0 (commercial use allowed)
- **Performance**: Optimized for mobile GPU inference

## Download Instructions

### Step 1: Get Model from Kaggle

1. Visit Kaggle Gemma Models: https://www.kaggle.com/models/google/gemma/tfLite
2. Navigate to the **gemma-2b-it** variant
3. Download the **INT8 quantized GPU-optimized** version:
   - File name: `gemma-2b-it-gpu-int8.task`
   - Size: approximately 1.5GB

### Step 2: Install Model

**Option A: Manual Installation (Testing)**
1. Connect your Android device via USB
2. Enable USB debugging
3. Copy model to device:
   ```bash
   adb push gemma-2b-it-gpu-int8.task /sdcard/Download/
   ```
4. Move to app directory:
   ```bash
   adb shell
   mv /sdcard/Download/gemma-2b-it-gpu-int8.task /data/data/com.lamontlabs.quantravision/files/llm_models/
   ```

**Option B: Production (Play Asset Delivery)**
For production release, bundle the model using Play Asset Delivery:
- Place model in `app/src/main/assets/models/gemma-2b-it-gpu-int8.task`
- Configure as install-time asset pack
- Model downloads during app installation

## File Format Requirements

⚠️ **CRITICAL**: Use the `.task` file format, NOT raw `.tflite`

MediaPipe requires pre-converted `.task` files that include:
- TFLite model graph
- Tokenizer vocabulary
- Model metadata
- Inference configuration

Raw TFLite files from other sources will **not work** with MediaPipe LLM Inference API.

## Verification

After installing the model, the app will:
1. Detect model file at startup
2. Load into memory on first AI request
3. Display "AI Ready" indicator in UI
4. Generate personalized explanations using on-device inference

## Fallback Behavior

If the model is not installed:
- App continues to work normally
- Uses template-based explanations instead
- No cloud API calls required
- 100% offline functionality maintained

## Model Updates

To update the model:
1. Download newer version from Kaggle
2. Replace existing `.task` file
3. Restart the app
4. New model loads automatically

## Privacy & Security

- **All inference happens on-device**
- No data sent to cloud servers
- No internet connection required
- Complete privacy guaranteed

## Troubleshooting

**Model not loading?**
- Verify file name is exactly: `gemma-2b-it-gpu-int8.task`
- Check file size is ~1.5GB
- Ensure file is in correct directory
- Restart app after copying file

**Out of memory errors?**
- Close other apps to free RAM
- Ensure device has 2GB+ available RAM
- Model requires ~1.8GB RAM when loaded

## License

Gemma 2B is licensed under Apache 2.0:
- ✅ Commercial use allowed
- ✅ Modification allowed
- ✅ Distribution allowed
- ✅ Private use allowed

Full license: https://www.apache.org/licenses/LICENSE-2.0
