# QuantraVision

## Overview
QuantraVision is an offline-first Android application that provides AI-powered chart pattern recognition for retail traders. It utilizes advanced OpenCV template matching to identify 109 technical analysis patterns in real-time. The app prioritizes user privacy through on-device processing and offers features such as predictive detection, multi-modal alerts, pattern invalidation warnings, and explainable AI with audit trails. It operates without subscriptions or cloud dependencies, offering a 4-tier one-time payment structure (Free, Starter, Standard, Pro) for lifetime access. Key features include an "Intelligence Stack" comprising the Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules, all designed for offline use with legal disclaimers for educational purposes.

## Working With Agent - Critical Guidelines

### Always Follow These Steps:
1. **Search the ENTIRE codebase first** before making any changes
2. **Verify ALL related files** - don't assume only one file needs changes
3. **Check git log** before assuming changes aren't committed (Replit auto-commits)
4. **Use GitHub Actions for builds** - Replit environment lacks Android SDK/tooling

### Paywall System Architecture (CRITICAL):
**Multiple files control paywall access - must modify ALL of them:**
- `BillingManager.kt` - Core billing logic
- `ProFeatureGate.kt` - Pro tier checks (used by UI)
- `StandardFeatureGate.kt` - Standard tier checks (used by UI)
- `StarterFeatureGate.kt` - Starter tier checks (used by UI)

**Common mistake:** Only modifying BillingManager - paywalls will still show because UI uses FeatureGates directly.

### Build Process:
- **DO:** Use GitHub Actions (`.github/workflows/android-ci.yml`)
- **DON'T:** Try to build on Replit (no Android SDK installed)
- **Build time:** ~5-10 minutes
- **APK location:** GitHub Actions artifacts → `quantravision-debug.zip`
- **APK size:** ~281MB (700MB uncompressed due to pattern images)

### Known Issues to Address Later:
- Overlay button touch-through issues (deferred)
- APK size optimization (pattern image compression needed)

### Common Commands:
```bash
# Push changes to trigger GitHub Actions build
git push origin main

# Check recent commits (Replit auto-commits)
git log --oneline -5

# Check what's committed but not pushed
git log origin/main..HEAD

# Uninstall app via ADB
adb uninstall com.lamontlabs.quantravision
```

## Recent Changes

### November 6, 2025 - UI Simplification
**STATUS: IN PROGRESS** - Removed bloated features, focused on core value proposition.

**Dashboard Reduced from 15 → 8 Features:**

**KEPT:**
1. Start Detection (core feature)
2. View Detections (results)
3. Intelligence Hub (unique differentiator: Regime Navigator, Pattern-to-Plan, Behavioral Guardrails, Proof Capsules)
4. Predictions (pattern forecasting)
5. Tutorials (interactive education)
6. Trading Book ("The Friendly Trader" by Jesse J. Lamont)
7. Achievements (gamification)
8. Settings (configuration)

**REMOVED:**
- Pattern Analytics (overcomplicated)
- Backtesting (not core value)
- Similarity Search (edge case feature)
- Multi-Chart Comparison (overcomplicated)
- Template Manager (developer tool)
- Voice Commands FAB (buggy, non-essential)
- Clear All Detections (moved to Settings later)

**UI Sections:**
1. Detection (2 buttons)
2. Intelligence Stack (1 button → opens 4 features)
3. Insights (1 button - Predictions)
4. Learn & Progress (3 buttons - Tutorials, Book, Achievements)
5. Settings (1 button)

**Rationale:** User wanted focused product, not feature bloat. Keep education (all tutorials + book), keep gamification (achievements), keep unique value (Intelligence Stack), remove everything else.

### November 6, 2025 - Paywall Bypass for Testing
**STATUS: COMPLETE** - Disabled all paywalls to enable full feature testing.

**Changes Made:**
- Added `BYPASS_PAYWALLS = true` to all 4 paywall control files:
  - `BillingManager.kt` (line 26)
  - `ProFeatureGate.kt` (line 19)
  - `StandardFeatureGate.kt` (line 19)
  - `StarterFeatureGate.kt` (line 19)
- All `isActive()` and tier checking methods now return `true` when flag is set
- Enables testing of Intelligence Stack, Trading Book, and all 109 patterns without purchases

**Lesson Learned:**
Initial attempt only modified BillingManager.kt - paywalls still appeared because UI screens use FeatureGate objects directly. Must modify ALL paywall control files, not just one.

**Next Steps:**
1. Push changes to GitHub (`git push origin main`)
2. Wait for GitHub Actions build (~5-10 min)
3. Download APK from artifacts
4. Uninstall old app completely (signature mismatch prevents update)
5. Install new APK with all paywalls disabled

### November 5-6, 2025 - Restoration to Working Version
**STATUS: COMPLETE** - After 200+ builds and crashes, identified working version from Nov 3rd evening.

**Critical Discovery**:
- App was **working perfectly** the evening of Nov 3rd (commit `9e46f69` - "Add scrollability to dashboard screen")
- Everything **broke Nov 4-5** due to:
  - 700MB of pattern template images causing OutOfMemory crashes on startup
  - App loading all 109 patterns × 6MB each = instant crash on Samsung S23 FE (Android 14)
  - Multiple failed attempts to fix with minimal builds, pattern compression, etc.

**Working Version Identified**:
- **Commit:** `9e46f69` (Nov 3rd evening / early Nov 4th 01:XX)
- **Features:** Full scrollable dashboard UI, overlay detection works, achievements work, all navigation functional
- **UI State:** Professional cyan theme, organized sections, smooth scrolling
- **Status:** App launches, buttons work, shows paywalls/empty states as expected for free tier

**Build History Context**:
- Build #207 and earlier: Various crash attempts
- Build #208-212: Failed attempts with SDK updates, duplicate class fixes, AAR errors
- Build #213: Successfully restored to commit `b9ce702` (basic working UI but no scrolling)
- Build #214: **CONFIRMED WORKING** - Successfully restored to scrollable UI from Nov 3rd evening

**Build #214 Verification (Nov 5, 2025)**:
✅ App launches without crashes on Samsung S23 FE (Android 14)
✅ Beautiful onboarding flow with icons and tutorials
✅ Scrollable dashboard with organized sections (Detection, Intelligence Stack, Analytics & Tools, Learn & Progress)
✅ Start Detection button launches overlay successfully
✅ Achievements system fully functional (0/50 unlocked)
✅ Educational content works (Triangle, Rectangle, Head & Shoulders patterns with ASCII diagrams)
✅ Trading Book paywall correctly shows pricing ($4.99 standalone, FREE with Standard/Pro tiers)
✅ Settings screen functional (logo size, opacity, theme controls)
✅ Professional cyan theme (#00E5FF) throughout entire app
✅ Voice command button accessible (bottom right FAB)

**Next Steps**:
1. ✅ Build #214 confirmed stable - use as baseline
2. Add improvements ONE AT A TIME with testing between each
3. Consider optimizations: pattern image compression, performance tuning, additional features

### November 4, 2025 - Critical UX Improvements
**STATUS: BUILD SUCCESSFUL** - User-reported UX issues resolved, all navigation entry points preserved.

Addressed three critical user-reported problems:
- **Onboarding Enhancement**: Added step-by-step overlay usage tutorial to DETECTION step explaining: 1) Tap "Start Detection" 2) Grant screen capture permission 3) Open trading app while overlay runs 4) Patterns highlighted with cyan border
- **Overlay Launch Fix**: Fixed "Start Detection" button in AppScaffold to properly start OverlayService with SYSTEM_ALERT_WINDOW permission checks and correct API level handling (startForegroundService vs startService)
- **Dashboard Reorganization**: Made dashboard fully scrollable, organized 15+ buttons into logical sections (Detection, Intelligence Stack, Analytics & Tools, Learn & Progress, Settings) with clear dividers, made "Start Detection" prominent, preserved all navigation callbacks (Templates, Backtesting, Multi-Chart, Clear All Detections, etc.)

**Architect Review**: Passed on second iteration - all navigation entry points accessible, overlay service implementation correct.

### November 4, 2025 - Build Success! (77 Errors Fixed)
**STATUS: BUILD SUCCESSFUL** - Zero compilation errors, APK generated successfully.

Fixed all 77 Kotlin compilation errors (58 initial + 18 additional + 1 final):
- **Database Model Usage**: Corrected PatternMatch property references (id, timeframe) across ProofCapsuleGenerator, WatchlistScanner, and other files
- **Paint API Conflicts**: Resolved Paint.Style vs PatternStyle naming conflicts in EnhancedOverlayRenderer using qualified references
- **UI Component Errors**: Fixed missing imports (Icons, LocalDensity, color extensions), duplicate function definitions (SummaryItem, EducationalDisclaimer), and Compose syntax issues
- **PaywallScreen Critical Fix**: Corrected book access entitlement logic to properly respect BillingManager.hasBook() for all tiers (prevented unauthorized access bug)
- **Export Functionality**: Fixed ExportViewModel database queries to use correct DAO methods
- **Pattern Library**: Updated SimilaritySearchScreen to use PatternLibrary.patterns.map{it.name} instead of non-existent method
- **PatternCard Intelligence Stack**: Fixed class references (RegimeContext→MarketRegime, TradePlan→TradeScenario) and method names (hasAccess→isActive)
- **Coroutine Safety**: Made WatchlistScanner.getTopOpportunities() suspend to properly call async database operations
- **ProofCapsuleGenerator Parameter Order**: Fixed PatternCard.kt to pass screenshot parameter (null) before regimeContext
- **GitHub Workflow APK Upload**: Updated android-ci.yml to upload correct universal APK filename (app-universal-debug.apk)

**Build Result**: All tasks completed successfully in 6m 15s. APK artifacts now properly uploaded to GitHub Actions.

## User Preferences
Preferred communication style: Simple, everyday language.

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with the Material 3 Design System, optimized for a dark theme, featuring a declarative and reactive UI. It utilizes ViewModel for state management, a modular screen architecture, responsive layouts, custom home screen widgets, and a consistent brand identity.

### Technical Implementations
**Pattern Detection Engine**: Employs an OpenCV template matching system for 109 chart patterns, coordinated by a HybridDetectorBridge and utilizing a BayesianFusionEngine for confidence scoring. Includes DeltaDetectionOptimizer and TemporalStabilizer, with TensorFlow Lite infrastructure for future ML enhancements.
**Scan Learning Engine**: (PRO tier only) Learns from chart scans to track pattern frequency, co-occurrence, and confidence distributions using perceptual image hashing for privacy-preserving, offline learning with adaptive threshold optimization.
**Data Storage**: Utilizes an encrypted Room database for local storage of logs, user preferences, achievements, and scan learning data.
**State Management**: Implements Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Manages a four-tier lifetime access model via Google Play In-App Billing, secured with Google Play Integrity API, signature verification, and R8/ProGuard obfuscation.
**AI/ML Architecture**: Primarily OpenCV-based, with TensorFlow Lite infrastructure for future Apache 2.0 licensed ML model integration.
**Alert System**: A centralized AlertManager provides voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay with a minimal, pulsing cyan border upon pattern detection, offering touch-passthrough to the underlying trading app.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions, with resource optimizations for OpenCV.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, logging detections with SHA-256 hashes, signing pattern catalogs with Ed25519, and maintaining an SBOM.

## External Dependencies

### Core ML/CV Libraries
- **OpenCV**: Computer vision for template matching and image processing.
- **TensorFlow Lite**: Infrastructure for future ML enhancements.

### Android Framework
- **Kotlin**: Primary programming language.
- **Jetpack Compose**: UI framework.
- **Room**: Local database persistence.
- **Android Architecture Components**: ViewModel, LiveData, WorkManager.
- **Material 3**: Design system.
- **MediaProjection**: Screen capture APIs.

### Utilities
- **Gson**: JSON parsing.
- **Google Play Billing**: In-app purchase handling.
- **Google Play Integrity API**: Anti-tamper verification.

### Offline Assets
- **Pattern Templates**: 109 PNG reference images and YAML configurations.
- **Legal Documents**: HTML/Markdown for terms and privacy policy.
- **Educational Content**: Interactive lessons.

### Security & Compliance
- **Ed25519 Cryptography**: Digital signatures.
- **SHA-256 Hashing**: Integrity verification.
- **Play Integrity API**: Runtime device and app verification.