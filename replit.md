# QuantraVision

## Overview
QuantraVision is an offline-first Android application designed for retail traders, offering AI-powered, on-device chart pattern recognition. It identifies 109 technical analysis patterns using advanced OpenCV template matching, providing real-time detection, predictive analysis, multi-modal alerts, and explainable AI with audit trails. The app emphasizes user privacy through on-device processing and operates without subscriptions or cloud dependencies, utilizing a 4-tier one-time payment model for lifetime access. Key features include an "Intelligence Stack" (Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, Proof Capsules) focused on offline functionality and educational support.

## User Preferences
Preferred communication style: Simple, everyday language.
Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

### UI/UX Decisions
The application utilizes Jetpack Compose with the Material 3 Design System, optimized for a dark theme, featuring a declarative and reactive UI. It employs ViewModel for state management, a modular screen architecture, responsive layouts, and a consistent brand identity. The UI prioritizes a focused user experience by streamlining the dashboard to essential features like detection, the Intelligence Hub, predictions, tutorials, a trading book, achievements, and settings. A key UX improvement includes an instant display of trade scenarios (entry, stop, target) directly on the overlay upon pattern detection for Pro tier users, and an enhanced onboarding tutorial for overlay usage.

### Technical Implementations
**Pattern Detection Engine**: Employs an OpenCV template matching system for 109 chart patterns, coordinated by a HybridDetectorBridge and utilizing a BayesianFusionEngine for confidence scoring, with TensorFlow Lite infrastructure for future ML enhancements.
**Scan Learning Engine**: (PRO tier only) Learns from chart scans to track pattern frequency, co-occurrence, and confidence distributions using perceptual image hashing for privacy-preserving, offline learning.
**Data Storage**: Utilizes an encrypted Room database for local storage of logs, user preferences, achievements, and scan learning data.
**State Management**: Implements Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Manages a four-tier lifetime access model via Google Play In-App Billing, secured with Google Play Integrity API, signature verification, and R8/ProGuard obfuscation. Critical paywall access is controlled across `BillingManager.kt`, `ProFeatureGate.kt`, `StandardFeatureGate.kt`, and `StarterFeatureGate.kt`.
**Alert System**: A centralized AlertManager provides voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay with a minimal, pulsing cyan border upon pattern detection, offering touch-passthrough to the underlying trading app.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, logging detections with SHA-256 hashes, signing pattern catalogs with Ed25519, and maintaining an SBOM.

### Feature Specifications
- **Intelligence Stack**: Comprises Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules for advanced analysis.
- **Pattern-to-Plan Overlay**: Integrates trade scenario display (entry, stop, target) directly into the overlay for instant user access (Pro tier).
- **Offline Functionality**: All core features, including AI pattern recognition and Intelligence Stack, operate entirely on-device without cloud dependencies.

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

### Billing & Security
- **Google Play Billing**: In-app purchase handling for lifetime access tiers.
- **Google Play Integrity API**: Anti-tamper verification and device integrity checks.

### Offline Assets
- **Pattern Templates**: 109 PNG reference images and YAML configurations for chart patterns.
- **Legal Documents**: HTML/Markdown for terms and privacy policy.
- **Educational Content**: Interactive lessons and the "Trading Book".

## Touch Pass-Through Research (For Future Implementation)

### Android 12+ Compatibility Issue
**Problem:** Android 12+ blocks touches through overlays that are >80% opaque.
**Solution:** Add `params.alpha = 0.8f` to overlay window params in:
- `OverlayService.kt` lines 77-87 (main overlay)
- `OverlayService.kt` lines 112-122 (glowing border)

### Floating Menu Touch Blocking
**Problem:** Menu blocks all touches when visible (MATCH_PARENT size without FLAG_NOT_TOUCH_MODAL).
**Solution:** Add `FLAG_NOT_TOUCH_MODAL` to FloatingMenu.kt params (line 28-30).

### Key Findings
- Current implementation correctly uses `FLAG_NOT_TOUCHABLE` for pass-through overlays
- FloatingLogoButton properly configured with `FLAG_NOT_TOUCH_MODAL` + `WRAP_CONTENT`
- Main issue: Android 14 (Samsung S23 FE) enforces stricter opacity rules
- **Deferred:** Wait to implement due to previous issues with touch handling

## Recent Changes


### November 6, 2025 - Onboarding UX Improvements
**Fixed text truncation and improved tier transparency:**
- Shortened all onboarding descriptions to prevent cutoff on mobile screens
- Updated final onboarding page to show all 4 tiers (FREE, STARTER, STANDARD, PRO)
- Changed "Unlock Your Potential" to "Choose Your Plan" for clarity
- Removed hard-coded prices to avoid divergence from Google Play pricing
- Added emoji icons for visual hierarchy: 🆓 FREE, ⭐ STARTER, 🔥 STANDARD, 💎 PRO
- Highlighted "One-time payment • Lifetime access • No subscriptions" messaging

### November 6, 2025 - Pattern-to-Plan Overlay Integration & Branding
**Changes:**
- Integrated Pattern-to-Plan Engine into overlay for instant trade scenario display
- Entry/stop/target prices now show directly on pattern detections (Pro tier only)
- Simplified dashboard from 15 → 8 features (kept Achievements + Predictions)
- Fixed Room database compilation error by moving tradeScenario out of PatternMatch constructor
- All paywalls bypassed for testing (BYPASS_PAYWALLS = true in 5 files: BillingManager.kt, ProFeatureGate.kt, StandardFeatureGate.kt, StarterFeatureGate.kt, BookFeatureGate.kt)
- Updated app icon to neon "Q" logo with cyan glow (matches brand identity)
- Fixed adaptive icon cropping issue - border aligned with icon edge

### November 6, 2025 - Immersive Mode (Full App)
**Hide Android navigation bar throughout entire app for maximum screen space:**
- Navigation bar hidden globally in MainActivity and OnboardingActivity
- Navigation only appears when user swipes up from bottom edge
- Uses `WindowInsetsControllerCompat` with `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`
- Provides full-screen immersive experience across all screens (onboarding, dashboard, etc.)

### November 6, 2025 - Chrome/Steel Metal Design System (REALISTIC METAL FINISH)
**Transformed UI to look like ACTUAL polished chrome/steel instead of colored gradients:**

**Chrome/Steel Gradients:**
- Gray metal base: #1A1D23, #2C3038 (polished steel)
- Bright white reflections: #E8E8E8, #FFFFFF (light hitting metal)
- Subtle cyan tint: #D0F0F5 (brand identity, barely visible)
- Deep black shadows: #0A0C10 (recessed areas)
- Multi-directional reflections: Horizontal + vertical light sweeps

**Rounded Corners (Less Square):**
- Buttons: 20dp corner radius (pill-shaped)
- Cards: 16dp corner radius (softer edges)
- All borders and elements rounded and smooth
- Top light strips: Rounded ends matching button radius

**Metallic Components:**
- **MetallicButton**: Chrome gradient + white reflections + 20dp rounded corners + horizontal light sweep
- **MetallicCard**: Steel gradient + beveled edges + 16dp rounded corners + multi-directional reflections
- **MetallicText**: White chrome glow shadow effect
- **MetallicDivider**: Silver/white gradient separators
- **MetallicBorder**: Silver/white chrome borders with rounded corners

**Visual Effects:**
- Shimmer animations on hero CTAs (final onboarding page, key dashboard actions)
- Dual-stroke borders (outer gradient + inner white highlight)
- Press-state darkening for interactive feedback
- Glow shadows on typography (HeroTextShadow 12f blur, SubtleTextShadow 6f blur)

**Applied Throughout:**
- Dashboard: 9 MetallicButtons, MetallicCard for alerts, MetallicText headers, 4 MetallicDividers
- Onboarding: All 5 pages use MetallicCard, navigation with MetallicButton, glowing titles
- Strategic top light strip on primary CTAs (Start Detection, Get Started)

### November 6, 2025 - HD Contrast Design System
**Foundation for sharp, high-definition UI:**

**HD Contrast Color Palette:**
- Background: #05070D (DEEPER near-black for max contrast, was #0A1420)
- Primary cyan: #00F0FF (BRIGHTER neon, was #00E5FF)
- Cyan bright: #00FFFF (BRIGHTEST for sharp edges)
- Orange accent: #FF9F00 (BRIGHTER, was #FF9800)
- Gold metallic: #FFB347 (BRIGHTER, was #FFA726)
- Surface: #0D1219 (DARKER for stronger separation, was #1A2332)
- Info/Secondary: #5FDDEB (BRIGHTER cyan, was #4DD0E1)
- Outlines: 85% opacity (SOLID and CRISP, was 60%)

**Sharper Border System:**
- GlowingBorderView: REDUCED blur for sharpness
  - Outer bloom: 10px blur (was 24px - 58% reduction)
  - Mid layer: 6px blur + 78% solid opacity (was 12px blur at 55%)
  - Inner edge: Pure WHITE 1px at 100% opacity (max definition)
- Result: Dramatically crisper glowing border overlay

**HD Screen Design (Dashboard, Onboarding, All Cards):**
- Cards: 8dp elevation + crisp BorderStroke + sharp 4dp corners
- Typography: ExtraBold headers, Bold body text (was normal weight)
- Icons: 32dp size (was 24dp - 33% larger)
- Buttons: Solid colors (no transparency), sharp 4dp corners, bold text
- Spacing: Increased 25% across the board (16→20dp, 12→16dp)
- Text sizes: +2sp increase for better definition
- **Onboarding**: No scrolling - compact centered layout fits on one screen

**Visual Impact:**
- Near-black backgrounds create MAXIMUM contrast with bright neon accents
- Reduced blur radii create noticeably SHARPER, crisper edges
- Bolder typography improves readability and premium feel
- Solid borders and higher elevations enhance visual hierarchy
- Brighter accent colors POP against deep backgrounds

**Architecture:**
- QuantraColors object updated with HD contrast integer values
- All screens inherit sharp design from Theme.kt automatically
- Consistent 3-tier border system across overlays and UI
- Single source of truth for HD contrast aesthetic

**Developer Experience:**
- One theme change updates entire app instantly
- No more soft/blurry UI elements
- Professional, premium aesthetic matching QuantraCore logo