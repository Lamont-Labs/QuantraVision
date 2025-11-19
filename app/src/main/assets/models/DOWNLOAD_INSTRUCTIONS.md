# Gemma 3 1B Model Download Instructions

## Overview

QuantraVision uses **Google Gemma 3 1B Instruct** for on-device AI explanations. This model runs 100% offline on your device using MediaPipe LLM Inference API.

## Model Specifications

- **Model**: Gemma 3 1B Instruct (INT4 Quantized)
- **Format**: MediaPipe `.task` file (not raw TFLite)
- **Size**: ~529MB (3x smaller than Gemma 2B!)
- **License**: Apache 2.0 (commercial use allowed)
- **Performance**: Optimized for mobile GPU inference

## Download Instructions

### Step 1: Accept Gemma License (One-Time)

1. Visit: https://huggingface.co/litert-community/Gemma3-1B-IT
2. Click **"Access repository"** button
3. Sign in to HuggingFace (create free account if needed)
4. Accept Google's Gemma license terms (Apache 2.0)

### Step 2: Download Model File

Once you have access:

**Option A: Direct Download (Easiest)**
1. Go to: https://huggingface.co/litert-community/Gemma3-1B-IT/tree/main
2. Click on **`gemma-3-1b-it-int4.task`**
3. Click the **download** icon (↓)
4. Save file (529MB)

**Option B: Command Line (Advanced)**
```bash
# Install HuggingFace CLI
pip install huggingface_hub

# Login to HuggingFace
huggingface-cli login

# Download model
huggingface-cli download litert-community/Gemma3-1B-IT gemma-3-1b-it-int4.task --local-dir ./
```

### Step 3: Install Model

**Option A: Manual Installation (Testing)**
1. Connect your Android device via USB
2. Enable USB debugging in Developer Options
3. Copy model to device:
   ```bash
   adb push gemma-3-1b-it-int4.task /sdcard/Download/
   ```
4. Move to app directory:
   ```bash
   adb shell
   mv /sdcard/Download/gemma-3-1b-it-int4.task /data/data/com.lamontlabs.quantravision/files/llm_models/
   ```

**Option B: Production (Play Asset Delivery)**
For production release, bundle the model using Play Asset Delivery:
- Place model in `app/src/main/assets/models/gemma-3-1b-it-int4.task`
- Configure as install-time asset pack
- Model downloads during app installation from Play Store

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
2. Load into memory on first AI request (~2-3 seconds)
3. Display "AI Ready" indicator in DevBot/QuantraBot
4. Generate personalized explanations using on-device inference

## Fallback Behavior

If the model is not installed:
- App continues to work normally
- Uses template-based explanations instead
- No cloud API calls required
- 100% offline functionality maintained

## Model Updates

To update the model:
1. Download newer version from HuggingFace
2. Replace existing `.task` file
3. Restart the app
4. New model loads automatically

## Privacy & Security

- **All inference happens on-device**
- No data sent to cloud servers
- No internet connection required after download
- Complete privacy guaranteed

## Troubleshooting

**Model not loading?**
- Verify file name is exactly: `gemma-3-1b-it-int4.task`
- Check file size is ~529MB
- Ensure file is in correct directory
- Check logcat for initialization errors
- Restart app after copying file

**Out of memory errors?**
- Close other apps to free RAM
- Ensure device has 1GB+ available RAM
- Model requires ~700MB RAM when loaded (much less than Gemma 2B!)

**Access denied on HuggingFace?**
- Make sure you accepted the Gemma license
- Wait 5-10 minutes after accepting for access to propagate
- Try logging out and back in to HuggingFace

## Performance Notes

**Gemma 3 1B vs Gemma 2B:**
- ✅ **3x smaller** (529MB vs 1.5GB)
- ✅ **Faster loading** (~2s vs ~5s)
- ✅ **Less RAM** (700MB vs 1.8GB)
- ✅ **Faster inference** on mobile
- ⚠️ Slightly lower quality responses (still excellent for diagnostics)

For DevBot diagnostics and pattern explanations, Gemma 3 1B provides the best balance of size, speed, and quality.

## License

Gemma 3 1B is licensed under Apache 2.0:
- ✅ Commercial use allowed
- ✅ Modification allowed
- ✅ Distribution allowed
- ✅ Private use allowed

Full license: https://www.apache.org/licenses/LICENSE-2.0
