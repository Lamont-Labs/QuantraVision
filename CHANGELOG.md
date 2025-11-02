# Changelog

All notable changes to QuantraVision will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- 🎯 **Scan Learning Engine** - Learns from every chart scan
  - Pattern frequency tracking across all detections
  - Co-occurrence analysis to identify pattern sequences
  - Adaptive threshold optimization based on user feedback
  - 100% offline, privacy-preserving statistical learning
  
- 📊 **Enhanced Analytics Dashboard** - Comprehensive performance insights
  - Pattern success rate tracking with confidence intervals
  - Temporal analysis (best trading times)
  - Risk-adjusted metrics (Sharpe ratios, expected value)
  - Behavioral pattern detection (overtrading, fatigue)

### Changed
- 💰 **4-Tier Pricing Structure** - Expanded from 3 to 4 tiers for better accessibility
  - **FREE**: 10 essential patterns (basic overlay)
  - **STARTER**: $9.99 - 25 patterns + multi-timeframe detection + basic analytics (NEW)
  - **STANDARD**: $24.99 - 50 patterns + achievements + lessons + book + exports
  - **PRO**: $49.99 - 109 patterns + Intelligence Stack + AI Learning + Scan Learning Engine
  - **Philosophy**: One-time payment, lifetime access, NO subscriptions (vs competitors' $180-$1,400/year)

- 🎨 **Overlay Design Improvements** - Enhanced user experience
  - Touch-passthrough technology for full trading app interaction
  - Draggable floating logo with customizable size/opacity
  - Smart badge showing active pattern count
  - Expandable quick actions menu

### Fixed
- 🐛 **Memory Management** - Resolved potential leaks
  - Fixed OpenCV Mat lifecycle management
  - Improved bitmap recycling in detection pipeline
  - Enhanced resource pooling efficiency

---

## [1.0.0] - 2025-11-02

### Added - Initial Production Release

#### 🚀 **Core Pattern Detection**
- **109 Pattern Library** with professional-grade detection
  - Classic reversals: Head & Shoulders, Double Tops/Bottoms, Triple patterns
  - Continuation patterns: Flags, Pennants, Rectangles, Channels
  - Triangle patterns: Ascending, Descending, Symmetrical, Broadening
  - Wedge patterns: Rising, Falling, Broadening
  - Candlestick patterns: 30+ formations (Engulfing, Hammer, Stars, etc.)
  - Advanced patterns: Harmonic patterns (Gartley, Butterfly, Bat, Crab)
  
- **OpenCV Template Matching Engine**
  - Multi-scale detection (0.5x to 2.0x)
  - Consensus scoring across scales
  - Rotation-invariant matching (±15 degrees)
  - Lighting normalization for cross-platform compatibility

- **TensorFlow Lite Integration**
  - Ready for future ML enhancements
  - Optimized model loading pipeline
  - Power-efficient inference engine

#### 🎯 **Intelligence Stack** (Pro Tier)

- **📊 Regime Navigator**
  - On-device market condition analysis
  - Volatility classification (calm/volatile)
  - Trend strength detection (ranging/trending)
  - Liquidity estimation
  - Pattern success rate context ("High probability - favorable conditions")

- **🎯 Pattern-to-Plan Engine**
  - Educational trade scenarios for every detection
  - Entry price calculations (e.g., "neckline break at $152.30")
  - Stop loss recommendations (pattern invalidation levels)
  - Take profit targets (measured moves)
  - Position sizing guidance (R:R ratios)
  - Voice scenario narration with disclaimers

- **🛡️ Behavioral Guardrails**
  - Cool-down timers to prevent revenge trading
  - Loss streak warnings ("3 losses in a row - take a break")
  - Emotion detection via rapid activity bursts
  - Voice coaching for emotional trading prevention

- **📦 Proof Capsules**
  - Tamper-proof detection receipts with SHA-256 hashing
  - Shareable via QR code or Android share
  - Timestamp + pattern + confidence + chart hash
  - Community building tool (no cloud dependency)

#### 🧠 **10-Algorithm AI Learning System**

1. **Pattern Correlation Analyzer** - Pearson correlation, sequence prediction
2. **Market Condition Learning** - 5 regime states, success rate optimization
3. **Temporal Pattern Learning** - Chi-squared testing, time-of-day heatmaps
4. **Risk-Adjusted Performance** - Sharpe ratios, expected value, max drawdown
5. **Behavioral Pattern Detection** - Overtrading, revenge trading, fatigue
6. **Multi-Pattern Strategy Learning** - Portfolio optimization, diversity index
7. **Predictive Trend Forecasting** - Linear regression, confidence intervals
8. **Anomaly Detection** - Z-score analysis, performance shift alerts
9. **Gradient Descent Calibration** - ML threshold optimization
10. **Comprehensive Learning Reports** - Weekly/monthly PDF exports

#### 🎤 **Professional Features**

- **Voice Announcements** (Android TTS)
  - Pattern detection alerts
  - Formation progress updates
  - Invalidation warnings
  - High-confidence notifications
  - Hands-free operation

- **Haptic Feedback Patterns**
  - 2 buzzes: Bullish pattern detected
  - 3 buzzes: Bearish pattern detected
  - Long buzz: High confidence (>80%)
  - Double long: Pattern invalidated
  - Glanceable alerts without screen

- **Predictive Detection**
  - Early pattern identification (40-85% formation)
  - Formation velocity analysis
  - Next pattern prediction
  - Estimated completion time
  - Key level identification

- **Pattern Strength Scoring**
  - 🟢 Strong (80-100%): A+ to A grade
  - 🟡 Moderate (60-80%): B to C grade
  - 🔴 Weak (40-60%): D to F grade
  - Formation percentage tracking
  - Confidence calibration

- **Invalidation Detection**
  - Real-time pattern breakage monitoring
  - Neckline violations (H&S)
  - Triangle divergence alerts
  - Trendline breaks
  - Voice + haptic + visual warnings

- **Auto-Scanning Watchlist**
  - Proactive pattern monitoring
  - Bullish/bearish pattern counts
  - Pattern clustering analysis
  - Configurable scan intervals (default 5 min)
  - Top opportunities ranking

#### 🎓 **Education System**

- **25 Comprehensive Lessons**
  - Pattern fundamentals to advanced harmonics
  - Interactive quizzes with scoring
  - Certificate of completion
  - Progress tracking
  - Visual examples with explanations

- **📚 Integrated Trading Book: "The Friendly Trader"**
  - **Author:** Jesse J. Lamont (complete 10-chapter comprehensive guide)
  - **Value:** $20 (included FREE with Standard/Pro tiers)
  - **Format:** Beautiful in-app reader with progress tracking, search, and night mode
  - **100% offline** - No internet required, read anywhere
  - **Curriculum:** Beginner to intermediate traders
  - **Chapter Coverage:**
    - Chapters 1-4: Market fundamentals, candlestick charts, trading tools, risk management
    - Chapters 5-8: Pattern recognition, trading psychology, demo trading, long-term success
  - **Free Preview:** All users get Chapters 1-2 before upgrading
  - **Perfect Complement:** Learn the "why" behind patterns while QuantraVision handles the "what"

#### 🎮 **Gamification System**

- **15 Progressive Achievements**
  - First Detection, Pattern Master (10/25/50/all patterns)
  - Accuracy achievements (80%/90%)
  - Speed Demon, Streak achievements (7/30 days)
  - Early Adopter, Professional, Educator, Quiz Master

- **Daily Streak Tracking**
- **Bonus Highlights** for engagement
- **Milestone Rewards**
- **Progress Visualization**

#### 🛠️ **Advanced Tools**

- **Backtesting Engine**
  - CSV import for historical data
  - Pattern performance analysis
  - Win rate calculations
  - Average return metrics

- **Multi-Chart Comparison**
  - Correlation analysis across charts
  - Pattern clustering detection
  - Portfolio view

- **Pattern Similarity Search**
  - Find similar historical patterns
  - Visual matching algorithm
  - Performance comparison

- **PDF Report Generator**
  - Comprehensive detection reports
  - Pattern performance summaries
  - Watermark-free (Pro tier)
  - Professional formatting

- **Voice Commands**
  - 16 natural language commands
  - "Scan for patterns"
  - "Show bullish setups"
  - "Export findings to PDF"
  - Hands-free operation

#### 🎨 **UI/UX**

- **Jetpack Compose Material 3**
  - Modern, declarative UI
  - Smooth animations
  - Dark/light theme support
  - Adaptive layouts

- **Minimal Overlay Design**
  - Touch-passthrough technology
  - Draggable floating logo
  - Pattern highlights with labels
  - Smart badge (pattern count)
  - Detection status ring
  - Fully customizable (size, opacity, position)

- **Clean Architecture**
  - MVVM pattern
  - Repository pattern
  - UseCase layer
  - Clear separation of concerns

#### 🔒 **Privacy & Security**

- **100% Offline Operation**
  - Zero data collection
  - No analytics tracking
  - No cloud sync
  - All processing on-device
  - Complete privacy

- **Secure Billing**
  - Encrypted preferences
  - Google Play Billing v7
  - Retry logic for network failures
  - Fail-safe fallback (never lock out paying users)
  - Revenue protection

- **Legal Compliance**
  - Fail-closed disclaimer system
  - Financial advice disclaimers
  - Educational tool positioning
  - Comprehensive ToS and Privacy Policy

#### 📱 **Platform Support**

- **Android Requirements**
  - Minimum: Android 7.0 (API 24)
  - Target: Android 15 (API 35)
  - Kotlin 2.1.0
  - 150 MB storage space

- **Performance**
  - Memory-efficient detection pipeline
  - Bitmap pooling to prevent leaks
  - Power-aware processing
  - Thermal throttling protection

#### 📦 **Deployment**

- **4-Tier Pricing Model**
  - FREE: 10 patterns, watermarked overlays, 5 lessons
  - STANDARD: $14.99 - 30 patterns, Regime Navigator, Free book
  - PRO: $29.99 - 109 patterns, Full Intelligence Stack
  - BOOK: $2.99 - Standalone trading book (free with Standard/Pro)

- **One-Time Purchase**
  - No subscriptions
  - No renewals
  - Lifetime access
  - Fair pricing promise

---

## Version History

### Version Naming Convention

QuantraVision follows [Semantic Versioning](https://semver.org/):
- **MAJOR**: Incompatible API changes
- **MINOR**: Backwards-compatible functionality additions
- **PATCH**: Backwards-compatible bug fixes

### Roadmap

**Planned for v1.1.0:**
- Multi-timeframe analysis improvements
- Enhanced pattern correlation algorithms
- Expanded voice command vocabulary
- Additional achievement milestones

**Under Consideration:**
- Dark pool volume analysis
- Order flow visualization
- Integration with broker APIs (user-requested)
- Custom pattern template editor

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for details on submitting changes.

## Support

- 📧 **Email**: jesse@lamont.click
- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/Lamont-Labs/QuantraVision/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/Lamont-Labs/QuantraVision/discussions)

---

<div align="center">

**QuantraVision** - Professional Pattern Detection for Traders

[![Production Ready](https://img.shields.io/badge/Status-Production%20Ready-success?style=flat-square)](PRODUCTION_CERTIFICATION.md)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

</div>
