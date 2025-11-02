# QuantraVision Repository Structure

**Lamont Labs - Professional Development Organization**  
**Last Updated:** October 31, 2025

---

## 📁 Repository Organization

This repository follows professional software engineering standards with clear separation of concerns, comprehensive documentation, and production-ready code organization.

---

## Root Directory (10 Files Only)

```
QuantraVision/
├── README.md                    # Project overview with badges and quick start
├── LICENSE                      # MIT License
├── CONTRIBUTING.md              # Contribution guidelines for developers
├── CODE_OF_CONDUCT.md           # Contributor Covenant code of conduct
├── SECURITY.md                  # Security policy and vulnerability reporting
├── build.gradle.kts             # Root Gradle build configuration
├── settings.gradle.kts          # Gradle settings
├── gradle.properties            # Gradle properties
├── gradlew                      # Gradle wrapper (Unix)
├── gradlew.bat                  # Gradle wrapper (Windows)
└── .gitignore                   # Comprehensive ignore patterns
```

**Design Principle:** Root directory contains only essential project files. All documentation, assets, and auxiliary files are organized in subdirectories.

---

## Core Directories

### `/app/` - Android Application Source

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/lamontlabs/quantravision/
│   │   │   ├── analysis/          # Pattern detection algorithms
│   │   │   ├── ui/                # Jetpack Compose UI components
│   │   │   ├── ml/                # Machine learning optimization
│   │   │   │   ├── optimization/  # Model loading, tensor pooling
│   │   │   │   ├── fusion/        # Bayesian fusion, temporal stability
│   │   │   │   ├── inference/     # Real-time pipeline
│   │   │   │   └── learning/      # Incremental learning
│   │   │   ├── education/         # 25-lesson course system
│   │   │   ├── gamification/      # Achievements, streaks
│   │   │   ├── alerts/            # Voice, haptic, visual alerts
│   │   │   └── ... (40+ packages)
│   │   ├── assets/                # Offline resources
│   │   │   ├── models/            # AI/ML models (YOLOv8, templates)
│   │   │   ├── pattern_templates/ # 119 OpenCV templates
│   │   │   └── education/         # Lesson content
│   │   └── res/                   # Android resources
│   ├── androidTest/               # Instrumented tests
│   └── test/                      # Unit tests
└── build.gradle.kts               # App module build configuration
```

**Architecture:** Modern Android development with Kotlin, Jetpack Compose, and offline-first design.

---

### `/docs/` - Comprehensive Documentation

```
docs/
├── ai/                            # AI/ML Documentation
│   ├── AI_ENHANCEMENT_ROADMAP.md  # 5-phase optimization plan
│   ├── AI_IMPLEMENTATION_STATUS.md # Current implementation status
│   ├── AI_INTEGRATION_REPORT.md   # Integration details
│   ├── AI_ENHANCEMENT_SUMMARY.md  # Executive summary
│   ├── MODEL_OPTIMIZATION_GUIDE.md # YOLOv8 quantization guide
│   └── HYBRID_DETECTION.md        # Hybrid detection methodology
│
├── tutorials/                     # User Tutorials
│   ├── PROFESSIONAL_USER_TUTORIAL.md # Complete user guide (1000+ lines)
│   ├── IN_APP_TUTORIAL_GUIDE.md   # In-app tutorial implementation
│   └── USER_GUIDE.md              # Quick start guide
│
├── development/                   # Developer Documentation
│   ├── CONFIG.md                  # Configuration guide
│   ├── OPERATIONS.md              # DevOps and deployment
│   ├── VALIDATION_GUIDE.md        # Quality assurance
│   ├── HANDOFF.md                 # Project handoff documentation
│   ├── LIMITATIONS.md             # Known limitations
│   ├── README-REPLIT.md           # Replit-specific setup
│   ├── FINAL_GITHUB_PUSH_REPORT.md # GitHub preparation
│   └── FINAL_QUALITY_CHECKLIST.md # Pre-release checklist
│
├── legal-reference/               # Legal Documentation Reference
│   ├── LEGAL_NOTICE.md            # Legal summary
│   ├── TERMS_OF_USE.md            # Terms of service
│   ├── NOTICE.md                  # Third-party notices
│   └── PROVENANCE.md              # Code provenance
│
├── CHANGELOG.md                   # Version history
├── RELEASE_NOTES.md               # Release announcements
├── REPOSITORY_STRUCTURE.md        # This file
└── ... (15+ other docs)
```

**Organization Principle:** Documentation is categorized by audience (AI/ML specialists, end users, developers, legal teams).

---

### `/legal/` - Active Legal Documents

```
legal/
├── FINANCIAL_DISCLAIMER.md        # 9 sections, California compliant
├── PRIVACY_POLICY.md              # CCPA compliant privacy policy
├── TERMS_OF_USE.md                # 16 sections, arbitration clause
├── LICENSE.md                     # MIT License with disclaimers
└── LEGAL_SUMMARY.md               # Legal protection assessment
```

**Status:** Production-ready, reviewed for California jurisdiction, 85/100 protection score.

---

### `/features/` - Feature Specifications

```
features/
├── FeatureSpec_*.yaml             # 20+ feature specifications
├── GapSpec_*.yaml                 # Gap analysis documents
└── Requirements.md                # Product requirements
```

**Methodology:** YAML-based feature tracking with clear objectives, architecture, and acceptance criteria.

---

### `/store_assets/` - Google Play Store Assets

```
store_assets/
├── feature_graphic_1024x500.png   # Play Store feature graphic
├── screenshots/                   # App screenshots
├── icon/                          # App icons (adaptive, legacy)
└── GOOGLE_PLAY_STORE_LISTING.md   # Store listing guide
```

**Status:** Production-ready assets optimized to exact Google Play specifications.

---

### `/pattern_templates/` - AI Pattern Templates

```
pattern_templates/
├── bullish/                       # 60+ bullish pattern templates
├── bearish/                       # 40+ bearish pattern templates
├── neutral/                       # Continuation patterns
└── README.md                      # Template documentation
```

**Format:** 119 high-quality OpenCV reference images for hybrid detection.

---

### `/scripts/` - Automation & Utilities

```
scripts/
├── build-debug.sh                 # Debug build script
├── build-release.sh               # Release build script
├── verify.sh                      # Signature verification
└── ... (deployment, testing scripts)
```

**Purpose:** CI/CD automation, local development utilities, verification tools.

---

### `/.github/` - GitHub Configuration

```
.github/
├── ISSUE_TEMPLATE/
│   ├── bug_report.md              # Structured bug reports
│   └── feature_request.md         # Feature request template
├── PULL_REQUEST_TEMPLATE.md       # PR guidelines
├── FUNDING.yml                    # Sponsorship configuration
└── workflows/                     # GitHub Actions (if applicable)
```

**Compliance:** Professional open-source project standards, Contributor Covenant alignment.

---

## Additional Directories

### `/compliance/` - Compliance Documentation
- Play Store compliance
- Google Play Integrity
- Backup retention policies

### `/security/` - Security Artifacts
- Security model
- Threat analysis
- Vulnerability reports

### `/qa/` - Quality Assurance
- Test plans
- QA checklists
- Performance benchmarks

### `/release/` - Release Management
- Release checklists
- Version planning
- Distribution artifacts

### `/monitoring/` - Observability
- Logging configuration
- Performance monitoring
- Error tracking

### `/ops/` - Operations
- Deployment procedures
- Infrastructure documentation
- Runbooks

### `/finance/` - Financial Documentation
- Pricing models
- Revenue tracking
- Billing integration

### `/provenance/` - Software Bill of Materials
- SBOM (JSON format)
- Dependency tracking
- License compliance

### `/tests/` - Additional Test Suites
- Integration tests
- Performance tests
- End-to-end tests

---

## Development Workflow

### 1. **Code Organization**
- **Package-by-feature** structure in `/app/src/main/java/`
- Clear separation: UI, business logic, data layer
- Dependency injection with Hilt (if applicable)

### 2. **Documentation Standards**
- All features have YAML specifications in `/features/`
- API documentation in code (KDoc)
- User-facing docs in `/docs/tutorials/`
- Developer docs in `/docs/development/`

### 3. **Version Control**
- Main branch: production-ready code
- Develop branch: integration branch
- Feature branches: `feature/feature-name`
- Release branches: `release/v1.x.x`

### 4. **Quality Gates**
- All PRs require code review
- Automated tests must pass
- Documentation must be updated
- Legal compliance verified

---

## Build & Deployment

### Build Artifacts
```
app/build/
├── outputs/
│   ├── apk/
│   │   ├── debug/           # Debug APK
│   │   └── release/         # Release APK (signed)
│   └── bundle/
│       └── release/         # App Bundle (Play Store)
└── reports/                 # Test reports, lint results
```

### Configuration Files
- `build.gradle.kts` - Build configuration
- `gradle.properties` - Build properties
- `proguard-rules.pro` - Code obfuscation
- `detekt.yml` (in `/config/`) - Static analysis

---

## Technology Stack

### Core Technologies
- **Language:** Kotlin 2.1.0
- **UI:** Jetpack Compose (Material 3)
- **Build:** Gradle 8.11.1, AGP 8.7.3
- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 15 (API 35)

### AI/ML Stack
- **ML Framework:** TensorFlow Lite 2.17.0 (Apache 2.0 licensed)
- **Computer Vision:** OpenCV 4.10.0 (Apache 2.0 licensed)
- **Pattern Detection:** Template matching (109 patterns)
- **Optimization:** GPU delegates, NNAPI, multi-threading

### Key Libraries
- Room 2.6.1 (local database)
- CameraX 1.5.0 (camera integration)
- Coroutines 1.10.1 (async operations)
- Navigation Compose 2.8.5 (navigation)
- Billing Library 8.0.0 (in-app purchases)

---

## Security & Privacy

### Privacy-First Design
- **100% Offline:** All AI processing on-device
- **No Telemetry:** Zero analytics or tracking
- **No Cloud:** No data ever transmitted
- **CCPA Compliant:** California privacy rights honored

### Security Measures
- ProGuard obfuscation for release builds
- ML Kit keep rules for model integrity
- Secure local storage (encrypted preferences)
- No hardcoded secrets or API keys

---

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for:
- Development setup
- Coding standards
- Pull request process
- Code review guidelines

See [CODE_OF_CONDUCT.md](../CODE_OF_CONDUCT.md) for community guidelines.

---

## Support & Contact

**Organization:** Lamont Labs  
**Developer:** Jesse J. Lamont  
**Email:** Lamontlabs@proton.me  
**GitHub:** github.com/Lamont-Labs/QuantraVision  
**License:** MIT (see [LICENSE](../LICENSE))

---

## Repository Metrics

| Metric | Value |
|--------|-------|
| **Total Lines of Code** | 50,000+ |
| **Kotlin Files** | 200+ |
| **Test Coverage** | Target 80%+ |
| **Documentation** | 100KB+ markdown |
| **Detection System** | OpenCV template matching |
| **Supported Patterns** | 109 chart patterns |
| **Lessons** | 25 interactive courses |

---

## Version History

See [CHANGELOG.md](../docs/CHANGELOG.md) for detailed version history.

---

**© 2025 Jesse J. Lamont | Lamont Labs**  
**Professional Software Engineering Standards**

**Repository Status:** ✅ Production-Ready | 🚀 GitHub-Ready | 💎 Lab-Quality
