# QuantraVision Development Guide

<div align="center">

**🛠️ Complete Developer Setup and Workflow Guide**

*Everything you need to build, test, and contribute to QuantraVision*

[![Android Studio](https://img.shields.io/badge/Android%20Studio-Ladybug%202024.2.1+-3DDC84?style=flat-square&logo=android-studio)](https://developer.android.com/studio)
[![JDK](https://img.shields.io/badge/JDK-17+-007396?style=flat-square&logo=java)](https://adoptium.net/)
[![Gradle](https://img.shields.io/badge/Gradle-8.11.1-02303A?style=flat-square&logo=gradle)](https://gradle.org/)

</div>

---

## 📋 Table of Contents

- [Prerequisites](#-prerequisites)
- [Development Environment Setup](#️-development-environment-setup)
- [Building the Project](#-building-the-project)
- [Running Tests](#-running-tests)
- [Debugging](#-debugging)
- [Code Style & Linting](#-code-style--linting)
- [Project Structure](#-project-structure)
- [Common Development Tasks](#-common-development-tasks)
- [Troubleshooting](#-troubleshooting)
- [Best Practices](#-best-practices)

---

## 📦 Prerequisites

### Required Software

| Tool | Minimum Version | Recommended | Download Link |
|------|----------------|-------------|---------------|
| **Android Studio** | Ladybug 2024.2.1 | Latest Stable | [Download](https://developer.android.com/studio) |
| **JDK** | 17 | JDK 17 (LTS) | [Adoptium](https://adoptium.net/) |
| **Git** | 2.30+ | Latest | [Download](https://git-scm.com/) |
| **Gradle** | 8.11.1 | (via wrapper) | Included in project |

### Hardware Requirements

**Minimum:**
- 8 GB RAM
- 4 GB disk space (for Android SDK + project)
- Intel i5 or equivalent processor

**Recommended:**
- 16 GB RAM (for smooth emulator performance)
- 10 GB disk space
- Intel i7/AMD Ryzen 7 or better
- SSD for faster builds

### Android SDK Components

Install these via Android Studio SDK Manager:

- ✅ **Android SDK Platform 35** (Android 15)
- ✅ **Android SDK Build-Tools 35.0.0**
- ✅ **Android Emulator** (latest)
- ✅ **Android SDK Platform-Tools** (latest)
- ✅ **Intel x86 Emulator Accelerator (HAXM)** (for Intel CPUs)

---

## 🛠️ Development Environment Setup

### Step 1: Clone the Repository

```bash
# HTTPS
git clone https://github.com/Lamont-Labs/QuantraVision.git
cd QuantraVision

# SSH (if you have SSH keys configured)
git clone git@github.com:Lamont-Labs/QuantraVision.git
cd QuantraVision
```

### Step 2: Open in Android Studio

1. **Launch Android Studio**
2. **File → Open**
3. **Navigate to `QuantraVision` directory**
4. **Click "OK"**
5. **Wait for Gradle sync** (2-5 minutes on first run)

**Gradle Sync Status:**
```
✅ BUILD SUCCESSFUL in 3m 12s
```

If you see errors, check [Troubleshooting](#-troubleshooting) section.

### Step 3: Configure SDK

**If Android Studio prompts for SDK installation:**

1. Click **"Open SDK Manager"**
2. Install **Android 15 (API 35)** platform
3. Install **Build Tools 35.0.0**
4. Click **"Apply"** and wait for download

### Step 4: Verify Setup

Run the validation script:

```bash
bash scripts/validate-project.sh
```

**Expected Output:**
```
✅ Gradle wrapper found
✅ Android SDK configured
✅ Kotlin plugin detected
✅ OpenCV native libraries present
✅ Pattern templates validated (102 patterns)
✅ TensorFlow Lite models validated
✅ Zero LSP errors
✅ Build configuration valid

🎉 Project validation PASSED!
```

---

## 🔨 Building the Project

### Debug Build

```bash
# Command line
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
```

**Build Time:** ~2-3 minutes (incremental builds: ~30 seconds)

### Release Build

```bash
# Command line
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

**Note:** Release builds require a signing keystore (not included in the repo for security).

### Clean Build

```bash
# Remove all build artifacts
./gradlew clean

# Clean + fresh build
./gradlew clean assembleDebug
```

### Build Variants

```bash
# List all build variants
./gradlew tasks --all | grep assemble

# Build specific variant
./gradlew assembleDebug        # Debug APK
./gradlew assembleRelease      # Release APK (signed)
```

---

## 🧪 Running Tests

### Unit Tests (JUnit)

```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew app:testDebugUnitTest

# Run tests with coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

**Test Reports:**
- HTML: `app/build/reports/tests/testDebugUnitTest/index.html`
- XML: `app/build/test-results/testDebugUnitTest/`

### Instrumented Tests (Espresso)

```bash
# Prerequisites: Connect Android device or start emulator

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.lamontlabs.quantravision.integration.DetectionFlowTest
```

**Test Reports:**
- HTML: `app/build/reports/androidTests/connected/index.html`

### Test Coverage

```bash
# Generate coverage report
./gradlew testDebugUnitTest jacocoTestReport

# View coverage report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

**Target Coverage:** 80% for business logic

---

## 🐛 Debugging

### Logcat Filtering

**In Android Studio:**

```
# Filter by tag
tag:QuantraVision

# Filter by package
package:com.lamontlabs.quantravision

# Filter by log level
level:ERROR

# Combined filter
tag:QuantraVision level:WARN
```

**Command Line:**

```bash
# Real-time logs
adb logcat -s QuantraVision:V

# Save logs to file
adb logcat > quantravision_logs.txt

# Clear logcat buffer
adb logcat -c
```

### Timber Logging

```kotlin
// Use Timber for structured logging
import timber.log.Timber

class PatternDetector {
    fun detect(image: Bitmap) {
        Timber.d("Starting detection on image: ${image.width}x${image.height}")
        
        try {
            val patterns = analyze(image)
            Timber.i("Detected ${patterns.size} patterns")
        } catch (e: Exception) {
            Timber.e(e, "Detection failed")
        }
    }
}
```

**Log Levels:**
- `Timber.v()` - Verbose (development only)
- `Timber.d()` - Debug (development only)
- `Timber.i()` - Info
- `Timber.w()` - Warning
- `Timber.e()` - Error

### Breakpoints

**Conditional Breakpoints:**

1. Set breakpoint (click left gutter)
2. Right-click breakpoint
3. Add condition: `patterns.size > 5`

**Logpoint Breakpoints:**

1. Right-click breakpoint
2. Uncheck "Suspend"
3. Check "Log evaluated expression"
4. Enter: `"Patterns detected: " + patterns.size`

---

## 📝 Code Style & Linting

### Kotlin Style Guide

Follow [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ GOOD
class PatternDetector(
    private val context: Context,
    private val config: DetectionConfig
) {
    suspend fun detect(image: Bitmap): List<Pattern> = withContext(Dispatchers.Default) {
        // Implementation
    }
}

// ❌ BAD
class patterndetector(context:Context,config:DetectionConfig){
    fun detect(image:Bitmap):List<Pattern>{
        // Implementation
    }
}
```

### Linting

```bash
# Run lint checks
./gradlew lint

# View lint report
open app/build/reports/lint-results-debug.html

# Fix auto-fixable issues
./gradlew lintFix
```

**Lint Configuration:** `app/lint.xml`

### Code Formatting

**Android Studio Auto-Format:**

- **Format File**: `Ctrl+Alt+L` (Windows/Linux) or `Cmd+Option+L` (Mac)
- **Optimize Imports**: `Ctrl+Alt+O` (Windows/Linux) or `Cmd+Option+O` (Mac)

**Settings:**
- Code Style → Kotlin → Set from → Kotlin style guide

---

## 📁 Project Structure

```
QuantraVision/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lamontlabs/quantravision/
│   │   │   │   ├── ui/                  # Jetpack Compose screens
│   │   │   │   ├── detection/           # Pattern detection engine
│   │   │   │   ├── ml/                  # Machine learning
│   │   │   │   ├── learning/            # AI learning system
│   │   │   │   ├── intelligence/        # Intelligence Stack
│   │   │   │   ├── billing/             # In-app purchases
│   │   │   │   ├── analytics/           # Performance tracking
│   │   │   │   ├── education/           # Learning system
│   │   │   │   ├── gamification/        # Achievements
│   │   │   │   └── utils/               # Utilities
│   │   │   ├── res/
│   │   │   │   ├── drawable/            # Icons, images
│   │   │   │   ├── layout/              # XML layouts (legacy)
│   │   │   │   ├── values/              # Strings, colors, themes
│   │   │   │   └── ...
│   │   │   ├── assets/
│   │   │   │   ├── pattern_templates/   # 102 pattern YAML + PNG
│   │   │   │   ├── models/              # TFLite models
│   │   │   │   ├── legal/               # ToS, Privacy Policy
│   │   │   │   └── book/                # Trading book content
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                        # Unit tests
│   │   └── androidTest/                 # Instrumented tests
│   ├── build.gradle.kts                 # App-level Gradle config
│   └── proguard-rules.pro               # ProGuard configuration
├── gradle/
│   └── wrapper/                         # Gradle wrapper files
├── docs/                                # Documentation
│   ├── ARCHITECTURE.md
│   ├── VISUAL_GUIDE.md
│   ├── DEVELOPMENT.md (this file)
│   └── ...
├── scripts/                             # Build scripts
│   └── validate-project.sh
├── .github/                             # GitHub templates
│   ├── ISSUE_TEMPLATE/
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── workflows/                       # CI/CD (if applicable)
├── .gitignore                           # Git ignore rules
├── build.gradle.kts                     # Root-level Gradle config
├── gradle.properties                    # Gradle properties
├── gradlew                              # Gradle wrapper (Unix)
├── gradlew.bat                          # Gradle wrapper (Windows)
├── settings.gradle.kts                  # Gradle settings
├── README.md                            # Project README
├── CONTRIBUTING.md                      # Contribution guide
├── LICENSE                              # Apache 2.0 license
└── CHANGELOG.md                         # Version history
```

---

## 🔧 Common Development Tasks

### Adding a New Pattern Template

1. **Create YAML file** in `app/src/main/assets/pattern_templates/`:

```yaml
# bull_flag_continuation.yaml
id: "bull_flag_continuation"
name: "Bull Flag (Continuation)"
category: "CONTINUATION"
bias: "BULLISH"
confidence_threshold: 0.65
min_detection_score: 0.60
temporal_stability_frames: 3
```

2. **Create reference image** (`bull_flag_continuation_ref.png`)
   - 200x200 px
   - Transparent background
   - Clear pattern structure

3. **Update pattern catalog** (`patterns.json`)

4. **Rebuild project** to load new template

### Adding a New Screen

1. **Create Composable** in `ui/screens/`:

```kotlin
// NewFeatureScreen.kt
@Composable
fun NewFeatureScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Feature") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Screen content
        }
    }
}
```

2. **Add navigation route** in `AppScaffold.kt`

3. **Add to navigation graph**

### Modifying Detection Algorithm

1. **Edit `detection/Detector.kt`**
2. **Update tests** in `test/java/.../detection/`
3. **Run unit tests**: `./gradlew test`
4. **Validate on real charts**
5. **Document changes** in code comments

### Adding Dependencies

**build.gradle.kts (app level):**

```kotlin
dependencies {
    // Add new dependency
    implementation("com.example:library:1.0.0")
}
```

**After adding:**

```bash
# Sync Gradle
./gradlew sync

# Verify build
./gradlew assembleDebug
```

---

## 🚨 Troubleshooting

### Common Issues

#### Issue: Gradle Sync Failed

**Error:**
```
Could not resolve all artifacts for configuration ':app:debugCompileClasspath'
```

**Solution:**
```bash
# 1. Clean project
./gradlew clean

# 2. Invalidate caches
# Android Studio → File → Invalidate Caches → Invalidate and Restart

# 3. Check internet connection (for dependency downloads)

# 4. If still failing, delete .gradle folder
rm -rf .gradle
./gradlew sync
```

---

#### Issue: OpenCV Native Libraries Not Found

**Error:**
```
java.lang.UnsatisfiedLinkError: dalvik.system.PathClassLoader couldn't find "libopencv_java4.so"
```

**Solution:**

1. Verify OpenCV AAR is in `app/libs/`
2. Check `build.gradle.kts` includes:
   ```kotlin
   implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
   ```
3. Clean and rebuild:
   ```bash
   ./gradlew clean assembleDebug
   ```

---

#### Issue: LSP Errors in Android Studio

**Error:**
```
Unresolved reference: Timber
```

**Solution:**

1. **Gradle Sync**: Click "Sync Now" in notification bar
2. **Reimport Project**: File → Sync Project with Gradle Files
3. **Invalidate Caches**: File → Invalidate Caches → Invalidate and Restart
4. **Check imports**: Verify import statement is correct

---

#### Issue: Emulator Won't Start

**Error:**
```
HAXM is not installed
```

**Solution (Intel CPUs):**

1. Download [Intel HAXM](https://github.com/intel/haxm/releases)
2. Install HAXM
3. Restart computer
4. Start emulator

**Alternative (ARM Macs):**

- Use Android emulator with ARM system images (no HAXM needed)

---

#### Issue: Tests Failing

**Error:**
```
java.lang.NullPointerException at PatternDetectorTest.kt:45
```

**Solution:**

1. **Check test setup** (`@Before` methods)
2. **Mock dependencies properly**
3. **Run with verbose output**:
   ```bash
   ./gradlew test --info
   ```
4. **Check test report**:
   ```bash
   open app/build/reports/tests/testDebugUnitTest/index.html
   ```

---

## ✅ Best Practices

### Code Quality

1. **✅ Write KDoc for public APIs**
   ```kotlin
   /**
    * Detects patterns in the provided chart image.
    *
    * @param image The chart image to analyze
    * @return List of detected patterns with confidence scores
    */
   fun detectPatterns(image: Bitmap): List<Pattern>
   ```

2. **✅ Use sealed classes for state**
   ```kotlin
   sealed class DetectionState {
       object Idle : DetectionState()
       object Loading : DetectionState()
       data class Success(val patterns: List<Pattern>) : DetectionState()
       data class Error(val message: String) : DetectionState()
   }
   ```

3. **✅ Prefer immutable data classes**
   ```kotlin
   data class Pattern(
       val id: String,
       val name: String,
       val confidence: Float
   ) // Immutable by default
   ```

4. **✅ Use coroutines for async operations**
   ```kotlin
   suspend fun fetchData() = withContext(Dispatchers.IO) {
       // I/O operations
   }
   ```

### Performance

1. **✅ Recycle bitmaps**
   ```kotlin
   val bitmap = BitmapFactory.decodeResource(resources, R.drawable.chart)
   // ... use bitmap
   bitmap.recycle()
   ```

2. **✅ Use Flow for reactive streams**
   ```kotlin
   val detections: Flow<List<Pattern>> = dao.getPatterns()
       .map { it.filter { pattern -> pattern.confidence > 0.7 } }
   ```

3. **✅ Lazy initialization**
   ```kotlin
   private val detector by lazy {
       PatternDetector(context, config)
   }
   ```

### Testing

1. **✅ Name tests descriptively**
   ```kotlin
   @Test
   fun `detect returns high confidence patterns when chart quality is good`() {
       // Test implementation
   }
   ```

2. **✅ Follow AAA pattern** (Arrange, Act, Assert)
   ```kotlin
   @Test
   fun testPatternDetection() {
       // Arrange
       val testImage = loadTestImage()
       
       // Act
       val results = detector.analyze(testImage)
       
       // Assert
       assertEquals(5, results.size)
   }
   ```

---

## 📚 Additional Resources

- [Android Developer Documentation](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [OpenCV Android Documentation](https://docs.opencv.org/master/d5/df8/tutorial_dev_with_OCV_on_Android.html)
- [TensorFlow Lite Guide](https://www.tensorflow.org/lite/android)

---

## 📞 Getting Help

**Stuck? Need assistance?**

1. 📖 **Read the Docs**: Check [ARCHITECTURE.md](ARCHITECTURE.md) and [CONTRIBUTING.md](../CONTRIBUTING.md)
2. 🔍 **Search Issues**: [GitHub Issues](https://github.com/Lamont-Labs/QuantraVision/issues)
3. 💬 **Ask in Discussions**: [GitHub Discussions](https://github.com/Lamont-Labs/QuantraVision/discussions)
4. 📧 **Email Support**: jesse@lamont.click

---

<div align="center">

**Happy Coding!** 🚀

<img src="quantravision-logo.png" alt="QuantraVision" width="100"/>

*Built with ❤️ by traders, for traders*

</div>
