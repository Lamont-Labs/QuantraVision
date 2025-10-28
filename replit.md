# QuantraVision - Android Build Project

## Overview
QuantraVision is an Android application that provides AI visual overlay for traders, detecting and highlighting chart patterns in real-time using on-device computer vision. The app is privacy-focused, deterministic, and operates entirely offline.

## Project Status
**Last Updated:** October 28, 2025

The build environment has been successfully configured with all necessary tools and dependencies. The project is currently encountering XML resource compilation issues that need to be resolved.

## Build Environment Setup (Completed ✅)

### Installed Components
1. **Java (GraalVM 22.3)** - Android development requires Java
2. **Android SDK** - Command-line tools installed at `$HOME/android-sdk`
   - Platform: android-34
   - Build tools: 34.0.0
   - All licenses accepted
3. **Gradle** - Wrapper properly configured (v8.10.2)
4. **System tools** - unzip (required for SDK extraction)

### Configuration Files Fixed
1. **scripts/ci-env.sh** - Auto-detects JAVA_HOME from the java binary in PATH
2. **scripts/setup-android-sdk.sh** - Fixed directory organization logic for Android SDK extraction
3. **gradle/wrapper/gradle-wrapper.jar** - Downloaded proper Gradle wrapper (43KB)

### XML Resource Files Fixed
1. **app/src/main/res/values-es/strings.xml** - Fixed closing tag
2. **app/src/main/res/font/quantra_display.xml** - Escaped `&` character in font query

## Current Issues

### French Strings Resource File
The French strings file (`app/src/main/res/values-fr/strings.xml`) is causing compilation errors:
- Error: "Invalid unicode escape sequence in string" or "Can not extract resource"
- Likely cause: Special characters (e.g., apostrophes, accented characters) not properly encoded
- File location: `app/src/main/res/values-fr/strings.xml`

**Recommended Solution:** Replace curly/smart apostrophes with straight apostrophes (`'`) or properly escape special characters in XML.

## Build Workflow
The project is configured with a workflow named "Build Android App" that runs:
```bash
bash scripts/build-debug.sh
```

This script:
1. Sets up environment variables (Android SDK paths, Java home)
2. Installs Android SDK if not present
3. Generates Gradle wrapper if missing
4. Builds debug APK with: `./gradlew assembleDebug lint test`

## Project Structure
- **app/src/main/** - Main application source code (Kotlin)
- **app/src/main/res/** - Android resources (layouts, strings, drawables, fonts)
- **scripts/** - Build automation scripts
- **gradle/** - Gradle wrapper and configuration
- **tests/** - Unit and instrumentation tests
- **docs/** - Project documentation
- **pattern_templates/** - Trading pattern definitions (YAML)

## Key Features (From README)
- 120+ deterministic chart patterns
- Offline indicator recognition (EMA, VWAP, RSI, MACD, Bollinger, etc.)
- Risk labeling engine
- Macro recorder for MTF actions
- Latency profiler HUD
- Provenance + SBOM signing
- Offline billing with tier gating (Free / Standard / Pro)

## Next Steps
1. Fix French strings XML encoding issues
2. Validate all other XML resource files for proper encoding
3. Complete the debug APK build
4. Run lint and tests
5. Generate release AAB for Play Store deployment

## Build Output
Debug APK will be generated at: `app/build/outputs/apk/debug/*.apk`

## Notes
- The project uses Gradle 8.10.2
- Target Android SDK: API 34
- Build tools version: 34.0.0
- All builds are deterministic and reproducible
- The app requires overlay permissions to function
