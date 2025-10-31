# Quick Start Guide

## Prerequisites
- Android Studio Ladybug (2024.2.1) or higher
- JDK 17+
- Android SDK API 35
- Minimum 4GB RAM

## Build Instructions

### 1. Clone Repository
```bash
git clone https://github.com/Lamont-Labs/QuantraVision.git
cd QuantraVision
```

### 2. Open in Android Studio
- File → Open → Select QuantraVision directory
- Wait for Gradle sync to complete

### 3. Build APK
```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Run on Device
- Connect Android device (API 26+) via USB
- Enable USB debugging
- Click Run (▶️) in Android Studio

## Development

### Project Structure
- `app/src/main/java/` - Kotlin source code
- `app/src/main/assets/` - ML models, pattern templates
- `docs/` - Comprehensive documentation
- `legal/` - Terms, privacy policy, disclaimers

### Key Technologies
- Jetpack Compose (UI)
- TensorFlow Lite (ML inference)
- OpenCV (computer vision)
- Room (local database)

## Release Build

```bash
./gradlew assembleRelease
```

Requires signing keystore (not included in repo).

## Testing

Run unit tests:
```bash
./gradlew test
```

## Support
- GitHub Issues: https://github.com/Lamont-Labs/QuantraVision/issues
- Email: support@lamontlabs.com
