@echo off
REM QuantraVision - AI Model Installation Script (Windows)
REM Installs Gemma 3 1B model to connected Android device

setlocal

set MODEL_FILE=gemma-3-1b-it-int4.task
set MODEL_SIZE=529MB
set DEVICE_PATH=/data/data/com.lamontlabs.quantravision/files/llm_models

echo =========================================
echo QuantraVision AI Model Installer
echo =========================================
echo.

REM Check if model file exists
if not exist "%MODEL_FILE%" (
    echo ERROR: Model file not found!
    echo.
    echo Please download the model first:
    echo 1. Visit: https://huggingface.co/litert-community/Gemma3-1B-IT
    echo 2. Accept the license (one-time)
    echo 3. Download: gemma-3-1b-it-int4.task (529MB)
    echo 4. Place in this directory: %CD%
    echo.
    exit /b 1
)

echo Found model file: %MODEL_FILE%
echo.

REM Check if ADB is installed
where adb >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: ADB not found!
    echo.
    echo Install Android Debug Bridge (ADB):
    echo Download from: developer.android.com/tools/releases/platform-tools
    echo.
    exit /b 1
)

echo ADB found
echo.

REM Check if device is connected
adb devices | findstr /C:"device" >nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: No Android device connected!
    echo.
    echo Connect your phone and enable USB debugging:
    echo 1. Connect phone via USB
    echo 2. Settings - About Phone - Tap 'Build Number' 7 times
    echo 3. Settings - Developer Options - Enable USB Debugging
    echo 4. Accept the debugging prompt on your phone
    echo.
    exit /b 1
)

echo Device connected
echo.
echo Installing AI model to device...
echo This may take a few minutes (uploading %MODEL_SIZE%)...
echo.

REM Push model to temporary location
echo Uploading model...
adb push "%MODEL_FILE%" /sdcard/Download/
if %ERRORLEVEL% neq 0 (
    echo.
    echo Upload failed! Check USB connection and try again.
    exit /b 1
)

echo Upload complete
echo.

REM Create app directory and move model
echo Installing to app directory...
adb shell "mkdir -p %DEVICE_PATH% && mv /sdcard/Download/%MODEL_FILE% %DEVICE_PATH%/"
if %ERRORLEVEL% neq 0 (
    echo.
    echo Directory creation failed. App might not be installed yet.
    echo Install the APK first, then run this script again.
    exit /b 1
)

echo Model installed successfully!
echo.
echo =========================================
echo Installation Complete!
echo =========================================
echo.
echo The AI model is now on your phone at:
echo %DEVICE_PATH%/%MODEL_FILE%
echo.
echo Next steps:
echo 1. Launch QuantraVision app
echo 2. Open DevBot or QuantraBot
echo 3. AI will load automatically (~2-3 seconds)
echo 4. You'll see 'AI Ready' indicator
echo.
echo Enjoy real on-device AI!
echo.

endlocal
