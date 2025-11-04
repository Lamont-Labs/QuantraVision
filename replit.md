# QuantraVision

## Overview
QuantraVision is an offline-first Android application that provides AI-powered chart pattern recognition for retail traders. It utilizes advanced OpenCV template matching to identify 109 technical analysis patterns in real-time. The app prioritizes user privacy through on-device processing and offers features such as predictive detection, multi-modal alerts, pattern invalidation warnings, and explainable AI with audit trails. It operates without subscriptions or cloud dependencies, offering a 4-tier one-time payment structure (Free, Starter, Standard, Pro) for lifetime access. Key features include an "Intelligence Stack" comprising the Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules, all designed for offline use with legal disclaimers for educational purposes.

## Recent Changes

### November 4, 2025 - Touch-Passthrough FIXED! 
**STATUS: PRODUCTION READY** - Overlay now has full touch-passthrough, allowing users to interact with trading app underneath.

Fixed critical bug where FloatingMenu blocked ALL screen touches:
- **Root Cause**: FloatingMenu was MATCH_PARENT (full screen) without FLAG_NOT_TOUCH_MODAL, blocking all touches even when hidden
- **Fix 1**: Added FLAG_NOT_TOUCH_MODAL to FloatingMenu window params so touches outside menu area pass through
- **Fix 2**: Modified hide() to actually REMOVE view from WindowManager instead of just hiding it (prevents persistent touch-blocking)
- **Fix 3**: Enhanced MediaProjection.Callback to call cleanupMediaProjection() immediately before stopSelf() for faster resource release

**Result**: Users can now:
- ✅ Tap charts, buttons, and sliders in TradingView/Webull while overlay runs
- ✅ Drag/click the floating Q button (60dp area remains interactive)
- ✅ See cyan border pulse when patterns detected
- ✅ Full touch control of trading app with minimal overlay interference

**Files Modified**: FloatingMenu.kt (window flags + show/hide lifecycle), OverlayService.kt (cleanup ordering)

### November 4, 2025 - MediaProjection Screen Capture IMPLEMENTED
**STATUS: FULLY FUNCTIONAL** - Overlay now captures and analyzes LIVE trading app screen in real-time.

Completely overhauled overlay system after user testing revealed it was only scanning demo files:
- **ScreenCaptureCoordinator**: New Compose-based coordinator handles MediaProjection permission flow using rememberLauncherForActivityResult, graceful error handling with clear user feedback
- **Live Screen Capture**: OverlayService now uses MediaProjection API with ImageReader (RGBA_8888 hardware-accelerated), VirtualDisplay for real-time screen capture at 2.5 fps (400ms throttling for battery optimization)
- **Resource Management**: Added MediaProjection.Callback to handle consent revocation (when user clicks "Stop sharing"), immediate cleanup of VirtualDisplay/ImageReader/MediaProjection resources in all error paths and onDestroy
- **Stop Detection Button**: Added red "Stop Detection" button to dashboard (right after Start Detection) - properly stops OverlayService and cleans up all resources
- **Efficient Bitmap Processing**: Image-to-Bitmap conversion with proper recycling to prevent memory leaks, feeds HybridDetectorBridge.detectPatternsOptimized() with live screen content
- **Demo Mode Fallback**: Preserved demo file scanning behind useDemoMode flag for testing without MediaProjection

**User Flow**: Tap "Start Detection" → Overlay permission check → MediaProjection permission dialog → Live screen capture begins → Patterns detected on real trading app → Floating Q button shows status → Tap "Stop Detection" or system "Stop sharing" to end

**Files Created**: ScreenCaptureCoordinator.kt
**Files Modified**: AppScaffold.kt, OverlayService.kt, DashboardScreen.kt

**Architect Review**: Passed with enhancements - MediaProjection lifecycle management correct and leak-free, callback handles revocation cleanly, resource cleanup immediate and comprehensive.

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