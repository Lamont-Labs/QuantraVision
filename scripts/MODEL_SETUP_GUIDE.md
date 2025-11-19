# AI Model Setup Guide - Gemma 3 1B

## Overview

QuantraVision uses a 529MB AI model that runs **100% offline** on your device. This guide explains how to get the AI working for both testing and production.

---

## 🎯 **Three Ways to Get AI Working**

### **Option 1: Mobile-Only Import (IN-APP)** 📱 **RECOMMENDED FOR YOU!**

Use this method if you're on mobile without a computer:

**Steps:**
1. Download model from HuggingFace to your phone
2. Install QuantraVision APK
3. Open app → tap "Import Model" button
4. Select downloaded file → AI ready!

**Time:** 3 minutes (no computer needed!)

---

### **Option 2: For Testing with Computer (Manual Install)** ⚡ FASTEST

Use this method if you're building/testing the app yourself with ADB:

**Steps:**
1. Download the model (one-time)
2. Run our helper script
3. Done! AI works immediately.

**Time:** 5 minutes

---

### **Option 2: For Production (Play Asset Delivery)** 🏆 PROFESSIONAL

Use this method for Play Store release:
- Model bundles with app automatically
- Users never see manual setup
- Downloads during app installation

**Time:** Needs Google Play Developer account setup

---

## 📱 **Option 1: Mobile-Only Import (Recommended!)**

### **Step 1: Download the Model to Your Phone**

1. On your phone, visit: https://huggingface.co/litert-community/Gemma3-1B-IT
2. Click **"Access repository"** (create free HuggingFace account if needed)
3. Accept Google's Gemma license terms (instant approval)
4. Click the **"Files"** tab
5. Tap **`gemma-3-1b-it-int4.task`** (529MB)
6. Download to your phone (goes to Downloads folder)

### **Step 2: Build and Install QuantraVision APK**

1. Build APK via GitHub Actions (or download from releases)
2. Install APK on your Samsung S23 FE
3. Open the app

### **Step 3: Import Model in the App**

1. Open **DevBot** or **QuantraBot** tab
2. You'll see a card: **"AI Model Not Found"**
3. Tap **"Import Model from Phone"** button
4. Android file picker opens
5. Navigate to **Downloads** folder
6. Select **`gemma-3-1b-it-int4.task`**
7. Tap **"Select"**

### **Step 4: Wait for Import to Complete**

A progress dialog appears showing:
- ✅ Percentage complete (0-100%)
- ✅ Transfer speed (MB/s)
- ✅ Time remaining
- ✅ Cancel button (if needed)

Takes **2-5 minutes** to copy 529MB file.

### **Step 5: AI Ready!**

When import completes:
- ✅ Dialog dismisses automatically
- ✅ "AI Model Not Found" card disappears
- ✅ AI becomes ready instantly
- ✅ You can immediately ask questions!

**Benefits:**
- 📱 No computer needed
- 🔒 No special permissions required
- 🔄 Survives phone rotation during import
- ⚡ Can navigate away - import continues in background
- 🎯 Zero configuration

---

## 💻 **Option 2: Quick Testing with ADB**

### **Step 1: Download the Model**

1. Visit: https://huggingface.co/litert-community/Gemma3-1B-IT
2. Click **"Access repository"** (create free HuggingFace account if needed)
3. Accept Google's Gemma license terms
4. Click the **"Files"** tab
5. Download **`gemma-3-1b-it-int4.task`** (529MB)
6. Save to this project directory (where this README is)

### **Step 2: Run the Installer Script**

**On macOS/Linux:**
```bash
cd scripts
./install-model.sh
```

**On Windows:**
```cmd
cd scripts
install-model.bat
```

The script will:
- ✅ Check if model file exists
- ✅ Verify your phone is connected
- ✅ Upload model to your device (529MB - takes 2-5 min)
- ✅ Install to the correct app directory
- ✅ Confirm installation success

### **Step 3: Test the AI**

1. Launch QuantraVision on your phone
2. Open **DevBot** or **QuantraBot** tab
3. AI loads automatically (~2-3 seconds first time)
4. You'll see **"AI Ready"** indicator
5. Ask a question to test real AI responses!

---

## 🏆 **Option 3: Production Setup (Play Asset Delivery)**

For Play Store releases, the model should download automatically during app installation.

### **What is Play Asset Delivery?**

Google's official way to ship large assets (like AI models) with your app:
- Model downloads during app install
- Managed by Play Store
- Users never do manual setup
- Supports different models per device architecture

### **Implementation Checklist:**

- [ ] Create asset pack module in `build.gradle`
- [ ] Configure install-time delivery mode
- [ ] Add model extraction logic in `GemmaEngine`
- [ ] Test with internal testing track on Play Console
- [ ] Monitor download success rates

**Documentation:**
https://developer.android.com/guide/playcore/asset-delivery

**Note:** This requires a Google Play Developer account and production APK signing.

---

## 🔧 **Troubleshooting**

### **"Model file not found" error**

- Make sure you downloaded **exactly**: `gemma-3-1b-it-int4.task`
- Place it in the `scripts/` directory
- Check file size is ~529MB

### **"No device connected" error**

- Connect phone via USB
- Enable **USB Debugging** in Developer Options
- Accept the debugging prompt on your phone
- Run: `adb devices` to verify connection

### **"Upload failed" error**

- Check USB cable (try a different one)
- Disable MTP/File Transfer mode, use "Charging only"
- Free up space on phone (need 1GB free)
- Try running script with `sudo` (Linux/Mac)

### **App crashes when loading AI**

- Check device has 1GB+ free RAM
- Close other apps before testing
- Check logcat: `adb logcat | grep Gemma`
- Verify model file isn't corrupted (re-download)

### **AI not responding**

- Wait 5-10 seconds on first load (model initialization)
- Check DevBot/QuantraBot shows "AI Ready" indicator
- Restart app if stuck in "Loading..." state
- Check logcat for MediaPipe errors

---

## 📊 **Model Specifications**

| Spec | Value |
|------|-------|
| **Model** | Gemma 3 1B Instruct |
| **Quantization** | INT4 |
| **Size** | 529MB |
| **RAM Usage** | ~700MB when loaded |
| **License** | Apache 2.0 (commercial use allowed) |
| **Context** | 8192 tokens |
| **Performance** | ~40-60 tokens/sec on Samsung S23 FE |

---

## 🔐 **Privacy & Security**

- ✅ **100% offline** after model download
- ✅ **No cloud API calls** ever
- ✅ **All processing on-device**
- ✅ **No data collection**
- ✅ **Open source model** (Apache 2.0)

---

## 📝 **Developer Notes**

### **Where Model Lives:**

- **Testing**: `/data/data/com.lamontlabs.quantravision/files/llm_models/gemma-3-1b-it-int4.task`
- **Production**: Extracted from Play Asset Delivery pack on first launch

### **Fallback Behavior:**

If model is missing:
- App uses template-based responses
- No errors shown to user
- Everything still works offline
- GemmaEngine returns `ExplanationResult.Unavailable`

### **Checking Model Status:**

```kotlin
val gemmaEngine = GemmaEngine.getInstance(context)
val result = gemmaEngine.initialize()

when (result.isSuccess) {
    true -> Log.d("AI", "Model loaded - real AI active")
    false -> Log.d("AI", "Model missing - using templates")
}
```

---

## 🚀 **Next Steps**

**For Testing:**
1. ✅ Download model from HuggingFace
2. ✅ Run `install-model.sh` script
3. ✅ Build APK via GitHub Actions
4. ✅ Test AI on your phone

**For Production:**
1. ⬜ Set up Play Asset Delivery
2. ⬜ Configure install-time pack
3. ⬜ Upload to Play Console internal testing
4. ⬜ Verify automatic download works
5. ⬜ Submit for production review

---

## 📚 **Additional Resources**

- **Model Download**: https://huggingface.co/litert-community/Gemma3-1B-IT
- **MediaPipe Docs**: https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference
- **Play Asset Delivery**: https://developer.android.com/guide/playcore/asset-delivery
- **ADB Setup**: https://developer.android.com/tools/adb

---

**Questions?** Check the main `DOWNLOAD_INSTRUCTIONS.md` in `app/src/main/assets/models/`
