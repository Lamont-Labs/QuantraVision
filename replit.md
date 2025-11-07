# QuantraVision

## Overview
QuantraVision is an offline-first Android application for retail traders, providing AI-powered, on-device chart pattern recognition. It identifies 109 technical analysis patterns using advanced OpenCV template matching, offering real-time detection, predictive analysis, multi-modal alerts, and explainable AI with audit trails. The app prioritizes user privacy through on-device processing and operates without subscriptions or cloud dependencies, utilizing a 4-tier one-time payment model for lifetime access. Key capabilities include an "Intelligence Stack" (Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, Proof Capsules) focused on offline functionality and educational support for an enhanced trading experience.

## User Preferences
Preferred communication style: Simple, everyday language.
Always Follow These Steps:
1. Search the ENTIRE codebase first before making any changes
2. Verify ALL related files - don't assume only one file needs changes
3. Check git log before assuming changes aren't committed (Replit auto-commits)
4. Use GitHub Actions for builds - Replit environment lacks Android SDK/tooling

## System Architecture

### UI/UX Decisions
The application uses Jetpack Compose with the Material 3 Design System, optimized for a dark theme, featuring a declarative and reactive UI. It employs ViewModel for state management, a modular screen architecture, responsive layouts, and a consistent brand identity using a chrome/steel metallic design system with high-contrast elements and a dual-font typography (Orbitron for headers, Space Grotesk for body). The UI streamlines the dashboard to essential features and integrates trade scenario display directly on pattern detection overlays for Pro tier users. Immersive mode is enabled app-wide to maximize screen space.

### Technical Implementations
**Pattern Detection Engine**: Utilizes an OpenCV template matching system for 109 chart patterns, coordinated by a HybridDetectorBridge, with a BayesianFusionEngine for confidence scoring. TensorFlow Lite infrastructure is planned for future ML enhancements.
**Scan Learning Engine**: (PRO tier only) Learns from chart scans using perceptual image hashing for privacy-preserving, offline learning.
**Data Storage**: Encrypted Room database for local storage of logs, user preferences, achievements, and scan learning data.
**State Management**: Android Architecture Components (ViewModels, Repository pattern, LiveData/Flow) for reactive state propagation.
**Authentication & Licensing**: Four-tier lifetime access model managed via Google Play In-App Billing, secured with Google Play Integrity API, signature verification, and R8/ProGuard obfuscation.
**Alert System**: Centralized AlertManager for voice, haptic, and visual alerts based on pattern strength.
**Real-Time Overlay System**: Uses MediaProjection API for live chart overlay with touch-passthrough functionality and visual cues upon pattern detection.
**Performance & Power Management**: An Adaptive Pipeline with a PowerPolicyApplicator adjusts performance based on device conditions.
**Compliance & Provenance**: Adheres to Greyline OS v4.3 standards, including logging, digital signing of pattern catalogs, and SBOM maintenance.

### Feature Specifications
- **Intelligence Stack**: Comprises Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, and Proof Capsules for advanced offline analysis.
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

## Recent Changes

### November 7, 2025 - Futuristic HUD UI Transformation
**Complete UI overhaul to match QuantraCore 3D logo aesthetic:**

**New Visual Components:**
- **NeonText**: Layered blur text with cyan bloom effect (#00F0FF) for glowing headers and labels
- **GlowingIcon**: Multi-layer icon glow with cyan highlights for enhanced visibility
- **CircularHUDProgress**: Gradient arc progress indicator with segmented tick markers
- **CircularDataRing**: Concentric ring visualization for HUD-style data display
- **GlassMorphicCard**: Frosted glass effect using RenderEffect blur (Android 12+) with gradient fallback
- **ParticleStarfield**: Animated glowing dots background for depth and motion
- **RadialGlowBackground**: Ambient cyan/purple radial gradients for atmospheric lighting
- **Multi-layer Shadow System**: 3-layer drop shadows for depth and 3D elevation

**Dashboard Transformation:**
- Hero title now uses pulsing NeonText with cyan glow
- Circular HUD decorative elements frame the hero CTA button
- Glass morphic cards wrap all three accordions with backdrop blur
- Particle starfield and radial glow backgrounds create immersive depth
- Maintains clean 3-section layout (Intelligence Stack, Detection, Insights)

**Accessibility Improvements:**
- Fixed critical TalkBack bug using clearAndSetSemantics on multi-layer components
- Screen readers now announce neon text/icons exactly once instead of 4x
- Decorative blur layers properly excluded from accessibility tree

**Color Palette Enhancements:**
- Added NeonCyan (#00F0FF) and NeonCyanBright (#5FDDEB) for glow effects
- Added NeonGold (#FFD700) for orange/gold accent highlights
- Maintained high contrast (AA/AAA) for readability

**Visual Result:**
- Premium futuristic HUD aesthetic matching QuantraCore logo
- Layered chrome rings, neon cyan glow, circular arc-based components
- Glass morphism with particle effects for depth
- Production-ready with accessibility compliance

### November 7, 2025 - UI Simplification & Improved Text Contrast
**Removed voice command complexity and improved button readability:**

**Design Improvements:**
- **Improved Metallic Gradients**: Replaced bright white reflections with cyan (#5FDDEB) for better text contrast
- **Darker Base Colors**: Metallic buttons now use darker base colors to ensure white text pops
- **Text Contrast Enhancement**: Added dark semi-transparent overlay (35% black) behind all button text
- **Explicit Text Color**: All button content now uses pure white (Color.White) via CompositionLocalProvider
- **Pressed State Improvement**: Dimmed metallic gradient now includes cyan hint instead of gray

**Code Simplification:**
- **Removed Voice Commands**: Eliminated ~150 lines of voice command code for simpler, cleaner codebase
- **No Microphone Permission**: Removed microphone icon, permission handling, and voice status messages
- **Cleaner Dashboard**: Simplified to just hero CTA + 3 chrome accordions + settings icon

**Visual Result:**
- Button text now has high contrast against metallic backgrounds
- Chrome aesthetic maintained with improved readability
- Clean, professional look without complexity
- Production-ready for Samsung S23 FE (Android 14)

### November 7, 2025 - Glass Morphic Card Readability Enhancement
**Improved accordion readability while maintaining futuristic aesthetic:**

**Readability Improvements:**
- **Reduced Blur**: Decreased GlassMorphicCard blur radius from 20f to 8f for subtle frosted glass effect
- **Increased Opacity**: Raised background alpha from 0.3f/0.4f to 0.65f/0.7f for better text contrast
- **Enhanced Borders**: Increased border opacity from 0.3f/0.4f to 0.5f/0.6f for clearer card definition
- **Consistent Styling**: All 3 dashboard accordions now use uniform blur/opacity settings

**Technical Changes:**
- Updated GlassMorphicCard default parameters for better out-of-box readability
- Applied explicit blur/opacity overrides to all accordion instances in DashboardScreen
- Maintained Android 12+ RenderEffect blur with fallback for older devices

**Visual Result:**
- Accordion text now clearly readable on Samsung S23 FE (Android 14)
- Preserved premium frosted glass aesthetic with subtle backdrop blur
- Maintained futuristic HUD theme with cyan/gold neon accents
- Balanced transparency and readability for optimal user experience