#!/bin/bash
# QuantraVision - AI Model Installation Script
# Installs Gemma 3 1B model to connected Android device

set -e

MODEL_FILE="gemma-3-1b-it-int4.task"
MODEL_SIZE="529MB"
DEVICE_PATH="/data/data/com.lamontlabs.quantravision/files/llm_models"

echo "========================================="
echo "QuantraVision AI Model Installer"
echo "========================================="
echo ""

# Check if model file exists
if [ ! -f "$MODEL_FILE" ]; then
    echo "❌ ERROR: Model file not found!"
    echo ""
    echo "Please download the model first:"
    echo "1. Visit: https://huggingface.co/litert-community/Gemma3-1B-IT"
    echo "2. Accept the license (one-time)"
    echo "3. Download: gemma-3-1b-it-int4.task (529MB)"
    echo "4. Place in this directory: $(pwd)"
    echo ""
    exit 1
fi

echo "✅ Found model file: $MODEL_FILE"
echo ""

# Check if ADB is installed
if ! command -v adb &> /dev/null; then
    echo "❌ ERROR: ADB not found!"
    echo ""
    echo "Install Android Debug Bridge (ADB):"
    echo "  macOS: brew install android-platform-tools"
    echo "  Linux: sudo apt install adb"
    echo "  Windows: Download from developer.android.com"
    echo ""
    exit 1
fi

echo "✅ ADB found"
echo ""

# Check if device is connected
DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo "❌ ERROR: No Android device connected!"
    echo ""
    echo "Connect your phone and enable USB debugging:"
    echo "1. Connect phone via USB"
    echo "2. Settings → About Phone → Tap 'Build Number' 7 times"
    echo "3. Settings → Developer Options → Enable USB Debugging"
    echo "4. Accept the debugging prompt on your phone"
    echo ""
    exit 1
fi

echo "✅ Device connected"
echo ""
echo "Installing AI model to device..."
echo "This may take a few minutes (uploading $MODEL_SIZE)..."
echo ""

# Push model to temporary location
echo "📤 Uploading model..."
adb push "$MODEL_FILE" /sdcard/Download/ || {
    echo ""
    echo "❌ Upload failed! Check USB connection and try again."
    exit 1
}

echo "✅ Upload complete"
echo ""

# Create app directory and move model
echo "📁 Installing to app directory..."
adb shell "mkdir -p $DEVICE_PATH && mv /sdcard/Download/$MODEL_FILE $DEVICE_PATH/" || {
    echo ""
    echo "⚠️  Directory creation failed. App might not be installed yet."
    echo "   Install the APK first, then run this script again."
    exit 1
}

echo "✅ Model installed successfully!"
echo ""
echo "========================================="
echo "Installation Complete! 🎉"
echo "========================================="
echo ""
echo "The AI model is now on your phone at:"
echo "$DEVICE_PATH/$MODEL_FILE"
echo ""
echo "Next steps:"
echo "1. Launch QuantraVision app"
echo "2. Open DevBot or QuantraBot"
echo "3. AI will load automatically (~2-3 seconds)"
echo "4. You'll see 'AI Ready' indicator"
echo ""
echo "Enjoy real on-device AI! 🧠"
echo ""
