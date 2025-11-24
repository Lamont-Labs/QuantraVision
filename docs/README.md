# QuantraVision Documentation Hub

**Central documentation index for QuantraVision Android application**

Welcome to the QuantraVision documentation. This page provides quick navigation to all project documentation organized by category.

---

## 📑 Quick Navigation

| Document | Description | Status |
|----------|-------------|--------|
| [Main README](../README.md) | Project overview and quickstart | ✅ Complete |
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture and design | ✅ Complete |
| [CONTRIBUTING.md](../CONTRIBUTING.md) | Contribution guidelines | ✅ Complete |
| [SECURITY.md](../SECURITY.md) | Security policy and vulnerability reporting | ✅ Complete |
| [RELEASE_PLAYBOOK.md](../RELEASE_PLAYBOOK.md) | Release and deployment procedures | ✅ Complete |

---

## 📚 Documentation Categories

### 🎯 User Guides

**For End Users:**

- **[USER_GUIDE.md](USER_GUIDE.md)** - Complete user manual for app features
  - Installation and setup
  - Pattern detection usage
  - Billing tiers and features
  - Educational content
  - Troubleshooting

- **[QUICKSTART.md](QUICKSTART.md)** - 5-minute quick start guide *(Planned)*
  - First-time setup
  - First pattern detection
  - Basic navigation

### 🛠️ Technical Documentation

**For Developers and Technical Reviewers:**

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture (MVVM, Clean Architecture)
  - Layer breakdown (UI, Domain, Data)
  - Apex Intelligence System (109 protocols)
  - Cloud Narration Pipeline
  - Quota Management (QuotaGate)
  - Performance Guardrails
  - Security architecture
  - Testing strategy (120+ tests)

- **[FUTURE_ARCHITECTURE.md](FUTURE_ARCHITECTURE.md)** - Advanced ML enhancement roadmap
  - Geometric pattern detection design
  - On-device LLM integration
  - Trait/microtrait system
  - Suppression memory and drift tracking

- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Public API reference *(Planned)*
  - Core classes (ApexEngineMobile, QuotaGate, CloudReasoner)
  - Data models
  - Repository interfaces

### 🏗️ Development Documentation

**For Contributors:**

- **[CONTRIBUTING.md](../CONTRIBUTING.md)** - Contribution guidelines
  - Development setup
  - Coding standards (Kotlin style guide)
  - Testing requirements
  - Pull request process
  - Issue reporting

- **[RELEASE_PLAYBOOK.md](../RELEASE_PLAYBOOK.md)** - Release management
  - Version numbering (semantic versioning)
  - Build types (debug vs release)
  - Signing configuration
  - ProGuard setup
  - Google Play Console upload
  - Rollback procedures

- **[DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md)** - Implementation plan
  - Phase-by-phase breakdown (Phases 0-6)
  - Timeline estimates
  - Prerequisites and dependencies
  - Risk assessment

- **[CHANGELOG.md](CHANGELOG.md)** - Version history and release notes
  - Feature additions
  - Bug fixes
  - Breaking changes

### 🔒 Security & Compliance

**For Security Reviewers:**

- **[SECURITY.md](../SECURITY.md)** - Comprehensive security policy
  - Privacy-first architecture
  - Fail-closed design patterns
  - Vulnerability reporting process
  - Third-party dependency management
  - Incident response procedures
  - Compliance (GDPR, CCPA)

- **[PRIVACY_POLICY.html](../app/src/main/assets/legal/PRIVACY_POLICY.html)** - Legal privacy policy
  - Data collection practices (zero for FREE tier)
  - User rights
  - Third-party services

- **[TERMS_OF_USE.html](../app/src/main/assets/legal/TERMS_OF_USE.html)** - Terms of service
  - Acceptable use policy
  - Disclaimer of financial advice
  - Liability limitations

### 🎓 Learning & Concepts

**For Understanding the System:**

- **[ai/APEX_CONCEPTS.md](ai/APEX_CONCEPTS.md)** - Apex intelligence explained
  - Protocol system (Omega, Tier, Learning)
  - Deterministic validation
  - Entropy detection
  - Suppression memory

- **[ai/CLOUD_NARRATION.md](ai/CLOUD_NARRATION.md)** - Cloud explanation system
  - Architecture overview
  - Quota management
  - LLM contract validation
  - Fallback mechanisms

- **[development/TESTING_GUIDE.md](development/TESTING_GUIDE.md)** - Testing best practices
  - Unit testing patterns
  - Integration testing
  - Performance benchmarking
  - Test coverage goals

---

## 🎯 Reading Paths by Audience

### For New Users

1. [Main README](../README.md) - Project overview
2. [USER_GUIDE.md](USER_GUIDE.md) - How to use the app
3. [SECURITY.md](../SECURITY.md) - Privacy and security info

### For Developers (New Contributors)

1. [Main README](../README.md) - Project overview
2. [CONTRIBUTING.md](../CONTRIBUTING.md) - Development setup
3. [ARCHITECTURE.md](ARCHITECTURE.md) - System design
4. [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md) - Implementation plan

### For Technical Reviewers / Investors

1. [Main README](../README.md) - Executive summary
2. [ARCHITECTURE.md](ARCHITECTURE.md) - Technical architecture
3. [SECURITY.md](../SECURITY.md) - Security audit
4. [FUTURE_ARCHITECTURE.md](FUTURE_ARCHITECTURE.md) - ML enhancement vision
5. [CHANGELOG.md](CHANGELOG.md) - Development history

### For Security Auditors

1. [SECURITY.md](../SECURITY.md) - Security policy
2. [ARCHITECTURE.md](ARCHITECTURE.md) - Security architecture section
3. [PRIVACY_POLICY.html](../app/src/main/assets/legal/PRIVACY_POLICY.html) - Legal policy
4. Source code review (ApexEngineMobile, QuotaGate, IntegrityChecker)

---

## 📂 Documentation Organization

```
docs/
├── README.md                       # This file - Documentation hub
├── ARCHITECTURE.md                 # System architecture (production)
├── FUTURE_ARCHITECTURE.md          # ML enhancement roadmap
├── USER_GUIDE.md                   # End-user manual
├── DEVELOPMENT_ROADMAP.md          # Implementation timeline
├── CHANGELOG.md                    # Version history
├── APEX_DOCUMENTATION_INDEX.md     # Apex-specific docs index
│
├── ai/                             # AI/ML documentation
│   ├── APEX_CONCEPTS.md            # Apex intelligence explained
│   ├── CLOUD_NARRATION.md          # Cloud explanation system
│   ├── PATTERN_DETECTION.md        # Detection algorithms
│   └── LEARNING_SYSTEM.md          # Suppression memory, drift tracking
│
├── development/                    # Development docs
│   ├── TESTING_GUIDE.md            # Testing best practices
│   ├── BUILD_GUIDE.md              # Build and deployment
│   ├── DEBUGGING.md                # Debugging tips
│   └── PERFORMANCE.md              # Optimization guidelines
│
└── design/                         # Design documentation
    ├── UI_GUIDELINES.md            # Material 3 design system
    ├── UX_PATTERNS.md              # User experience patterns
    └── WIREFRAMES.md               # UI mockups and flows
```

---

## 🔍 Documentation Standards

All documentation in this repository follows these standards:

### Formatting

- **Markdown**: All docs use GitHub-flavored Markdown
- **Headers**: Use `#` for top level, `##` for sections, `###` for subsections
- **Code blocks**: Use triple backticks with language identifier (```kotlin, ```bash, etc.)
- **Tables**: Use Markdown tables for structured data
- **Links**: Relative links for internal docs, absolute for external

### Structure

- **Table of Contents**: Include for documents >300 lines
- **Version Info**: Include "Last Updated" date at bottom
- **Examples**: Provide code examples for technical concepts
- **Diagrams**: Use ASCII art or reference external images

### Maintenance

- **Review Cycle**: Update docs quarterly or with major releases
- **Accuracy**: Ensure code examples compile and run
- **Completeness**: No TODOs in published documentation
- **Clarity**: Write for target audience (user, developer, reviewer)

---

## 📝 Contributing to Documentation

Documentation improvements are always welcome! See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

**Types of documentation contributions:**

- Fixing typos or grammar
- Clarifying ambiguous explanations
- Adding code examples
- Updating outdated information
- Translating to other languages
- Creating new guides for uncovered topics

**Process:**

1. Fork the repository
2. Make documentation changes
3. Test all code examples
4. Submit pull request with clear description

---

## 🔗 External Resources

### Official Android Documentation

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Material Design 3](https://m3.material.io/)

### Libraries Used

- [OpenCV Android](https://docs.opencv.org/master/d5/df8/tutorial_dev_with_OCV_on_Android.html)
- [TensorFlow Lite](https://www.tensorflow.org/lite)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

### Related Projects

- [QuantraCore Apex](https://github.com/Lamont-Labs/QuantraCore) - Desktop trading intelligence engine

---

## 📞 Support

**Questions about documentation?**

- Open an issue: [GitHub Issues](https://github.com/yourusername/quantravision/issues)
- Email: support@lamontlabs.com
- Read FAQ: [USER_GUIDE.md](USER_GUIDE.md)

---

**Last Updated**: November 24, 2025  
**Maintainer**: Lamont Labs  
**License**: Apache-2.0
