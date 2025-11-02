# Contributing to QuantraVision

<div align="center">

<img src="docs/quantravision-logo.png" alt="Contributing to QuantraVision" width="400"/>

**Thank you for your interest in contributing to QuantraVision!**

*This guide will help you get started with contributing to our professional-grade pattern detection system.*

[![Contributors](https://img.shields.io/badge/Contributors-Welcome-brightgreen?style=flat-square)](https://github.com/Lamont-Labs/QuantraVision/graphs/contributors)
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-brightgreen?style=flat-square)](http://makeapullrequest.com)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

</div>

---

## 📋 Table of Contents

- [Code of Conduct](#-code-of-conduct)
- [Getting Started](#-getting-started)
- [Development Setup](#️-development-setup)
- [Architecture Overview](#️-architecture-overview)
- [Coding Standards](#-coding-standards)
- [Testing Requirements](#-testing-requirements)
- [Pull Request Process](#-pull-request-process)
- [Release Process](#-release-process)
- [Recognition](#-recognition)

---

## 📜 Code of Conduct

Please read and follow our [Code of Ethics](CODE_OF_ETHICS.md). We are committed to providing a welcoming and inclusive environment for all contributors.

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or later
- **JDK**: 17 or higher
- **Android SDK**: API 35 (Android 15)
- **Gradle**: 8.11.1 (included via wrapper)
- **Git**: Latest stable version

### Development Setup

1. **Fork the repository**
   ```bash
   # Fork via GitHub UI, then clone your fork
   git clone https://github.com/YOUR_USERNAME/quantravision.git
   cd quantravision
   ```

2. **Open in Android Studio**
   - File → Open → Select `quantravision` folder
   - Wait for Gradle sync to complete
   - Accept any SDK download prompts

3. **Build the project**
   ```bash
   ./gradlew clean build
   ```

4. **Run on device/emulator**
   - Click Run (▶️) in Android Studio
   - Or use: `./gradlew installDebug`

---

## 🔄 Development Process

### Branch Naming

- `feature/pattern-detection` - New features
- `bugfix/camera-crash` - Bug fixes
- `docs/update-readme` - Documentation
- `refactor/detector-cleanup` - Code refactoring
- `test/backtesting-suite` - Test additions

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
feat: add harmonic pattern detection for Bat patterns
fix: resolve camera memory leak on Android 14
docs: update installation instructions
test: add unit tests for PatternDetector
refactor: extract ScaleSpace into separate module
```

### Development Workflow

1. **Create a branch** from `main`
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write clean, documented code
   - Follow Kotlin coding conventions
   - Add tests for new functionality

3. **Test locally**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Open a Pull Request**

---

## 🔍 Pull Request Process

### Before Submitting

- ✅ Code builds without errors
- ✅ All tests pass
- ✅ No new warnings introduced
- ✅ Code follows style guidelines
- ✅ Documentation updated (if applicable)
- ✅ CHANGELOG.md updated (for user-facing changes)

### PR Template

Your PR should include:

1. **Description**: What does this PR do?
2. **Motivation**: Why is this change needed?
3. **Testing**: How was this tested?
4. **Screenshots**: (if UI changes)
5. **Checklist**: Confirm all requirements met

### Review Process

1. Automated checks run (build, tests, lint)
2. Maintainers review code
3. Address feedback and requested changes
4. Once approved, PR will be merged

---

## 💎 Coding Standards

### Kotlin Style

Follow the [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Naming**: 
  - Classes: `PascalCase`
  - Functions/Properties: `camelCase`
  - Constants: `SCREAMING_SNAKE_CASE`

### Code Organization

```kotlin
// 1. Package declaration
package com.lamontlabs.quantravision.detection

// 2. Imports (grouped and sorted)
import android.content.Context
import androidx.compose.runtime.*

// 3. KDoc documentation
/**
 * Detects chart patterns using template matching.
 *
 * @param context Android application context
 * @param config Detection configuration parameters
 */
class PatternDetector(
    private val context: Context,
    private val config: DetectionConfig
) {
    // Implementation
}
```

### Documentation

- **Public APIs**: Must have KDoc comments
- **Complex logic**: Inline comments explaining "why"
- **TODOs**: Use `// TODO: description` format

```kotlin
/**
 * Analyzes an image for pattern detections.
 *
 * @param image The input image to analyze
 * @return List of detected patterns with confidence scores
 */
suspend fun analyze(image: ImageProxy): List<Detection> {
    // Implementation
}
```

---

## 🧪 Testing Guidelines

### Test Structure

```kotlin
class PatternDetectorTest {
    private lateinit var detector: PatternDetector
    
    @Before
    fun setup() {
        detector = PatternDetector(context, config)
    }
    
    @Test
    fun `detect bull flag pattern returns correct confidence`() {
        // Given
        val testImage = loadTestImage("bull_flag.png")
        
        // When
        val results = detector.analyze(testImage)
        
        // Then
        assertTrue(results.isNotEmpty())
        assertEquals("BULL_FLAG", results.first().id)
        assertTrue(results.first().confidence > 0.7)
    }
}
```

### Coverage Requirements

- **New features**: Minimum 70% coverage
- **Bug fixes**: Add test reproducing the bug
- **Critical paths**: 90%+ coverage

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Coverage report
./gradlew jacocoTestReport
```

---

## 🏗️ Project Structure

```
quantravision/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lamontlabs/quantravision/
│   │   │   │   ├── detection/     # Pattern detection engine
│   │   │   │   ├── ui/            # Compose UI screens
│   │   │   │   ├── prediction/    # Predictive intelligence
│   │   │   │   ├── gamification/  # Achievement system
│   │   │   │   ├── analytics/     # Performance tracking
│   │   │   │   └── education/     # Learning system
│   │   │   ├── res/               # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                  # Unit tests
│   └── build.gradle.kts
├── docs/                          # Documentation
├── scripts/                       # Build scripts
└── build.gradle.kts
```

---

## 🐛 Reporting Bugs

### Bug Report Template

**Title**: Brief description of the bug

**Description**: 
- What happened?
- What did you expect?

**Steps to Reproduce**:
1. Open app
2. Navigate to...
3. Click on...
4. See error

**Environment**:
- Android version: 
- Device model:
- App version:

**Screenshots**: (if applicable)

---

## 💡 Feature Requests

We welcome feature suggestions! Please:

1. Check if the feature already exists
2. Search existing issues/PRs
3. Open a new issue with:
   - Clear description
   - Use case/motivation
   - Expected behavior
   - Alternative solutions considered

---

## 📞 Questions?

- 💬 **Discord**: [Join Community](https://discord.gg/quantravision)
- 📧 **Email**: Lamontlabs@proton.me
- 📚 **Wiki**: [Developer Documentation](https://github.com/Lamont-Labs/QuantraVision/wiki)

---

## 🎉 Recognition

Contributors will be:
- Listed in CHANGELOG.md for their contributions
- Acknowledged in release notes
- Added to CONTRIBUTORS.md (with permission)

---

Thank you for contributing to QuantraVision! Your efforts help make advanced pattern recognition accessible to traders worldwide. 🚀
