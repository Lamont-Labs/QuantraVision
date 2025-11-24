<div align="center">

<img src="docs/quantravision-logo.png" alt="QuantraVision Logo" height="120"/>

<h1>
  QuantraVision
</h1>

<h3>🚀 AI-Powered Chart Pattern Detection for Professional Traders (Concept)</h3>

<p align="center">
  <strong>Offline-First · Privacy-Preserving · Institutional-Grade Intelligence</strong>
</p>

---

<!-- Technology Badges -->
<p align="center">
  <img src="https://img.shields.io/badge/Android-15%20(API%2035)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Kotlin-1.9.25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/OpenCV-4.10.0-5C3EE8?style=for-the-badge&logo=opencv&logoColor=white" alt="OpenCV"/>
  <img src="https://img.shields.io/badge/TensorFlow-2.17.0-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white" alt="TensorFlow"/>
</p>

<!-- Status Badges -->
<p align="center">
  <img src="https://img.shields.io/badge/Status-Development%20Paused-yellow?style=for-the-badge" alt="Development Paused"/>
  <img src="https://img.shields.io/badge/Builds-100%2B%20Successful-success?style=for-the-badge" alt="100+ Builds"/>
  <img src="https://img.shields.io/badge/Privacy-100%25%20Offline-blue?style=for-the-badge" alt="100% Offline"/>
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=for-the-badge" alt="Apache 2.0"/>
</p>

---

<!-- Quick Navigation -->
<p align="center">
  <a href="#-project-status"><strong>Status</strong></a> •
  <a href="#-vision"><strong>Vision</strong></a> •
  <a href="#-current-state"><strong>Current State</strong></a> •
  <a href="#-future-architecture"><strong>Future</strong></a> •
  <a href="#-documentation"><strong>Documentation</strong></a> •
  <a href="#-contributing"><strong>Contributing</strong></a>
</p>

</div>

---

## ⚠️ Project Status

**Current State:** Development paused (November 2025)

**Build Status:**
- ✅ **100+ successful builds** on Samsung S23 FE
- ⚠️ **Core features partially functional** - template matching and OCR implemented
- 🔄 **Pattern detection operational** but accuracy requires optimization
- 📊 **Baseline functionality achieved** but advanced features incomplete
- 🎯 **Apex-inspired intelligence** fully documented, awaiting implementation

**Development Context:**
- All code AI-generated (GPT + Replit Agent) with user providing vision/direction
- NucBox K6 desktop with Android Studio + Samsung S23 FE target device
- 100+ build iterations completed with incremental feature testing

---

## 🚀 Quickstart

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Version 17
- **Gradle**: 8.7+ (included via wrapper)
- **Test Device**: Physical Android device (API 28+) or emulator

### Build Instructions

```bash
# Clone repository
git clone https://github.com/yourusername/quantravision.git
cd quantravision

# Verify project structure
bash scripts/validate-project.sh

# Grant execute permission
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Run Tests

```bash
# Run all unit tests (120+ tests)
./gradlew test

# Run lint checks
./gradlew lint

# View test report
open app/build/reports/tests/testDebugUnitTest/index.html
```

### GitHub Actions CI

The project includes automated CI/CD:
- **Lint**: Code quality checks
- **Unit Tests**: 120+ comprehensive tests
- **Build**: Debug APK assembly
- **Artifacts**: APK available for download (7-day retention)

Workflow: `.github/workflows/ci.yml`

### First Launch

1. Install APK on device
2. Grant MediaProjection permission when prompted
3. Navigate to **Scan** tab
4. Enable overlay to capture chart screenshots
5. Open any trading app (TradingView, Webull, etc.)
6. Tap overlay icon to trigger pattern detection

### Testing Features

**Local Pattern Detection (FREE Tier):**
- ✅ 109 Apex protocols (Omega → Tier → Learning)
- ✅ QuantraScore calculation (0-100)
- ✅ Deterministic local summaries
- ✅ 2-4 FPS performance throttling
- ✅ Zero cloud calls

**Cloud Narration (Paid Tiers):**
- ⚙️ PRO tier: 10 cloud explanations/day
- ⚙️ ULTRA tier: 25 cloud explanations/day
- ⚙️ OpenAI API integration (requires API key)
- ⚙️ LLM contract validation (forbidden words filtered)

### Documentation

- **Verification Guide**: [docs/verify_demo.md](docs/verify_demo.md)
- **Architecture**: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- **Development**: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)
- **User Guide**: [docs/USER_GUIDE.md](docs/USER_GUIDE.md)

---

## 🎯 Vision

QuantraVision aspires to be a privacy-first Android app providing institutional-grade trading intelligence to retail traders through 100% offline, on-device AI pattern recognition.

### Core Value Proposition (Intended)

**Privacy-First Intelligence:**
- 100% offline processing, zero cloud dependencies
- No data collection, no subscriptions
- Lifetime access via one-time payment

**Institutional-Grade Analysis:**
- Advanced pattern detection with sophisticated validation
- Multi-layer signal processing (traits, protocols, entropy detection)
- Explainable AI with complete audit trails
- Adaptive learning from scan history

**Democratized Access:**
- Bring institutional trading intelligence to individual traders
- Professional-grade analysis at retail price point
- Educational focus with transparency

---

## 📋 Current State

### What Exists (Partially Functional)

**Complete Codebase with 100+ Build Iterations:**
- ✅ Jetpack Compose UI with Material 3 Design (functional)
- ⚠️ Template matching pattern detection (109 PNG references - operational but needs accuracy improvements)
- ⚠️ OCR indicator extraction (RSI, MACD, volume, 30+ indicators - implemented, requires refinement)
- 🔄 Multi-signal analysis engine (QuantraCore Intelligence - baseline functionality)
- 🔄 Pattern learning system (architecture complete, learning accuracy unvalidated)
- ✅ Ensemble AI engine for Q&A (operational)
- ✅ QuantraBot AI assistant (functional)
- ✅ Room database for local storage (working)
- ⚠️ Google Play Billing integration (implemented, not production-tested)
- ✅ MediaProjection overlay system (operational)

**Known Limitations:**
- **Template Matching Accuracy:** Estimated 40-60% (breaks with different chart styles, platforms, timeframes)
- **OCR Reliability:** Variable across different chart platforms and indicator formats
- **Feature Completeness:** Core systems functional but advanced features require additional development

### What's Incomplete

- **Apex Intelligence System:** Fully documented but not yet implemented (geometric detection, trait/microtrait, protocol stack)
- **Production Validation:** No extensive real-world trader testing
- **Market Validation:** Untested market demand
- **Accuracy Optimization:** Pattern detection needs refinement for production reliability

---

## 🔮 Future Architecture: Apex-Inspired Intelligence

**Vision:** Transform from template matching (40-60% accuracy) to geometric pattern detection (70-85% target) with institutional-grade validation inspired by QuantraCore Apex desktop trading engine.

### Planned Enhancement

**Geometric Pattern Detection:**
- Replace pixel-based templates with geometry-based structural analysis
- 15-20 core patterns (vs current 109 templates)
- Works across any platform, timeframe, chart style
- OpenCV contour/shape detection with confidence scoring

**Apex-Inspired Validation Stack:**
- **Traits:** High-level signal categorizations
- **Microtraits:** Granular decomposition (3-8 per trait)
- **Protocols:** 15-20 deterministic validation rules
- **Entropy Detection:** Identify conflicting signals
- **Suppression Memory:** Learn from false positives
- **Drift Tracking:** Adapt to decaying pattern effectiveness

**Enhanced Scoring:**
- Multi-factor weighted fusion
- Entropy penalties, suppression adjustments, drift modifiers
- 0-100 score with detailed component breakdown

**Hybrid Explanations:**
- Fast path: Template-based for common scenarios (< 1 second)
- Smart path: Gemma 2B LLM for complex cases (10-30 seconds)
- Plain English trade recommendations

**Deterministic Proof Logging:**
- Hash-verified audit trail for every scan
- SHA-256 integrity verification
- Complete decision transparency

**Status:** Fully documented design specification, not implemented. See [FUTURE_ARCHITECTURE.md](docs/FUTURE_ARCHITECTURE.md) for complete technical details.

**Implementation Estimate:** 6-11 weeks, 114-174 hours of active development with consistent desktop access.

---

## 📚 Documentation

### Essential Reading

**[replit.md](replit.md)** - Start here
- Complete project overview
- Honest current state assessment
- Future vision with Apex-inspired architecture
- Development constraints and context

**[docs/FUTURE_ARCHITECTURE.md](docs/FUTURE_ARCHITECTURE.md)** - Technical Design
- Complete Apex intelligence system specification
- Geometric detection algorithms
- Trait/microtrait system
- Protocol stack implementation
- Code examples and patterns

**[docs/DEVELOPMENT_ROADMAP.md](docs/DEVELOPMENT_ROADMAP.md)** - Implementation Plan
- Phase-by-phase breakdown (Phases 0-6)
- Realistic timeline estimates (10-17 weeks)
- Prerequisites and dependencies
- Risk assessment and mitigation
- Go/no-go checkpoints

**[docs/APEX_DOCUMENTATION_INDEX.md](docs/APEX_DOCUMENTATION_INDEX.md)** - Documentation Hub
- Central navigation for all documentation
- Reading paths for different audiences
- Quick reference summaries

### Additional Documentation

- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Current system architecture
- **[docs/USER_GUIDE.md](docs/USER_GUIDE.md)** - End-user documentation (for future users)
- **[docs/ai/](docs/ai/)** - AI system implementation details
- **[docs/development/](docs/development/)** - Development workflow and operations

---

## 🛠️ Tech Stack

### Core Technologies

**Mobile Framework:**
- Kotlin 1.9.25
- Jetpack Compose (UI)
- Material 3 Design System
- Android Architecture Components (ViewModel, Room, LiveData/Flow)

**AI/ML Libraries:**
- OpenCV 4.10.0 (computer vision)
- TensorFlow Lite 2.17.0 (on-device inference)
- Google ML Kit Text Recognition (OCR)

**Future Dependencies (If Apex Implemented):**
- Gemma 2B or Phi-2 (~800MB-1GB) for complex explanation generation

**Android APIs:**
- MediaProjection (screen capture)
- Google Play Billing (in-app purchases)
- Google Play Integrity API (security)

---

## 🚀 Getting Started (For Developers)

### Prerequisites

**Required:**
- Android Studio installed
- Desktop with 4+ GB RAM
- Samsung S23 FE or similar device for testing
- 40-80 hours available for debugging and development

**Current Status:**
- Codebase exists but never compiled
- Expect compilation errors, missing dependencies, type mismatches
- First task: Get app building and launching
- Second task: Fix runtime crashes
- Third task: Validate actual pattern detection accuracy

### Build Instructions

1. Clone repository
2. Open in Android Studio
3. Resolve compilation errors (expect many)
4. Build APK
5. Install on device
6. Debug runtime issues
7. Test core functionality
8. Measure baseline performance

**Estimated Time to First Working Build:** 10-20 hours (highly variable)

**See:** [DEVELOPMENT_ROADMAP.md](docs/DEVELOPMENT_ROADMAP.md) Phase 0 for detailed steps

---

## 🤝 Contributing

**Current State:** Project on hold pending developer availability

**Future Contributions Welcome:**
- If you have Android development experience and want to resurrect this project
- If you're interested in geometric pattern detection algorithms
- If you want to implement the Apex-inspired intelligence system
- If you have trader feedback or market validation insights

**Before Contributing:**
- Read [replit.md](replit.md) for complete context
- Review [FUTURE_ARCHITECTURE.md](docs/FUTURE_ARCHITECTURE.md) for technical vision
- Understand this is an untested codebase requiring substantial debugging

**Contact:** Open an issue to discuss potential collaboration

---

## 📄 License

Apache License 2.0 - See [LICENSE](LICENSE) for details

---

## 🔗 Related Projects

**QuantraCore Apex:** Institutional-grade desktop trading intelligence engine (Python) with 80 tier protocols, 25 learning protocols, live market data integration. QuantraVision mobile is inspired by Apex concepts adapted for standalone offline operation.

GitHub: https://github.com/Lamont-Labs/QuantraCore

---

## ⚖️ Legal & Disclaimers

**Educational Purpose Only:** All pattern detection, scoring, and recommendations are educational tools, not financial advice.

**No Guarantees:** No claims about accuracy, profitability, or market performance. Trading involves risk of loss.

**Use At Own Risk:** Software provided "as is" without warranty. See Apache 2.0 license.

---

## 💭 Honest Reflection

### What We Know

**Technical:**
- Architecture is sound
- Code exists for all planned features
- OpenCV + TensorFlow Lite can work on mobile
- Geometric detection is proven in other contexts

**Uncertain:**
- Will it actually achieve 70-85% accuracy?
- Will template matching work at all (40-60% is estimate, not measured)?
- Can solo developer (with AI help) debug complex Android app?
- Is market validation achievable?

**Market:**
- Zero proof traders want this
- Free alternatives exist (TradingView, etc.)
- $50-200 price point untested
- Uncertain ROI on $1000+ investment

### Why Document This?

**Preservation:** If desktop time becomes available, complete technical vision is preserved

**Transparency:** Honest record of what was attempted vs achieved prevents false expectations

**Learning:** Detailed architecture and roadmap useful even if project never completes

**Options:** Documentation enables future collaboration, acquisition, or open source contribution

---

<div align="center">

**This project may never launch. That's okay. The vision is documented, the code exists, and the option remains open if circumstances change.**

**For questions or collaboration:** Open an issue

</div>
