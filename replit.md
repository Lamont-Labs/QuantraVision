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

### November 7, 2025 - Professional Bottom Navigation Redesign
**Complete navigation architecture transformation from accordion layout to Material Design 3 bottom navigation:**

**New Bottom Navigation Structure:**
- **5-Tab Navigation Bar**: Material 3 NavigationBar with Home, Markets, Scan, Learn, Settings
- **Home Tab**: Quick stats cards, recent detections list, extended FAB for "Start Detection"
- **Markets Tab**: Pattern templates (109), Intelligence Hub, Predictions, Backtesting, Similarity Search, Multi-Chart View
- **Scan Tab**: Detection controls, how-it-works guide, feature highlights
- **Learn Tab**: Interactive tutorials, Trading Book, Achievements & progress tracking
- **Settings Tab**: Alert settings, floating logo preferences, developer options (clear database)

**Key Components:**
- `BottomNavScaffold.kt`: Main container with Material 3 NavigationBar orchestrating 5 primary destinations
- `HomeScreen.kt`: Modern dashboard with quick stats + recent detections + FAB
- `MarketsScreen.kt`: Intelligence & pattern library with all PRO features accessible
- `ScanScreen.kt`: Detection controls with professional onboarding flow
- `LearnScreen.kt`: Educational content hub with progress tracking
- `SettingsScreenWithNav`: Flexible settings supporting both bottom nav and standalone modes

**Navigation Improvements:**
- ✅ Professional app store quality layout matching TradingView, Robinhood patterns
- ✅ Material Design 3 compliance throughout
- ✅ Card-based information architecture (no accordions)
- ✅ All features from previous accordion layout preserved and accessible
- ✅ Extended FAB for primary action (Start Detection)
- ✅ Bottom navigation state restoration on reselection
- ✅ Single-top launch mode preventing duplicate screens

**Technical Architecture:**
- Replaced accordion-based DashboardScreen with professional bottom navigation pattern
- Archived old DashboardScreen.kt → DashboardScreen.old.kt for reference
- Bottom nav integrates seamlessly with existing NavHost for deep navigation
- Settings screen conditionally renders based on context (bottom nav vs standalone)
- All navigation callbacks properly wired through BottomNavScaffold → AppScaffold

**Visual Result:**
- Professional, modern Android app matching industry standards
- Clean information hierarchy with discoverable navigation
- Intuitive user experience following Material Design 3 guidelines
- Production-ready for Samsung S23 FE (Android 14)

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

### November 7, 2025 - Sharp HUD Aesthetic Transformation
**Replaced blurred glass with crisp edges and glowing borders to match QuantraCore logo aesthetic:**

**Design Transformation:**
- **Removed All Blur**: Eliminated backdrop blur completely (blurRadius: 20f → 0f) for razor-sharp text
- **Solid Backgrounds**: Increased opacity dramatically (0.3f → 0.85f alpha) for high contrast and readability
- **Multi-Layer Glow Borders**: Replaced blur with 3-layer cyan/gold glow system (outer 30%, mid 50%, inner 100% alpha)
- **Sharp Metallic Edges**: Clean rounded corners (12.dp) with crisp border definition
- **Customizable Intensity**: Added glowIntensity parameter (0.6f-0.8f) for per-card control

**Technical Implementation:**
- Complete GlassMorphicCard redesign using drawBehind for multi-layer border glow
- Removed Android 12+ RenderEffect blur dependency entirely
- Added CornerRadius import for precise rounded rectangle drawing
- All 3 dashboard accordions now use sharp aesthetic with NO blur
- Accordion 1 (Detection): cyan glow at 0.7f intensity
- Accordion 2 (Intelligence): gold glow at 0.8f intensity
- Accordion 3 (Learn): cyan glow at 0.7f intensity

**Visual Result:**
- Perfectly crisp, readable text matching QuantraVision logo reference
- Bright cyan/gold edge glow without content distortion
- Sharp metallic HUD aesthetic with professional polish
- High contrast dark backgrounds (0.85f alpha) for optimal readability
- Production-ready for Samsung S23 FE (Android 14)

### November 7, 2025 - Professional UI Redesign (App Store Quality)
**Complete UI simplification to eliminate amateur appearance and achieve professional app store quality:**

**Problem Addressed:**
- Visible "boxes around text" from multi-layer glow effects and borders
- Content getting cut off
- Over-designed, busy appearance with excessive blur layers
- Amateur look instead of professional app store quality

**Complete Border & Box Removal:**
- REMOVED all chrome borders (.metallicBorder calls)
- REMOVED all drawBehind rectangular overlays
- REMOVED all background boxes from expanded sections
- Result: ZERO visible borders or boxes anywhere in UI

**GlassMorphicCard Simplification:**
- REMOVED multi-layer glowing borders (3 layers creating visible boxes)
- REMOVED drawBehind border drawing completely
- Corner radius: 12.dp → 16.dp (softer)
- Padding: 12.dp → 16.dp (more breathing room)
- Spacing: 8.dp → 12.dp
- Background opacity: 0.85f → 0.90f (more solid, better readability)
- Clean borderless appearance

**MetallicButton Complete Cleanup:**
- REMOVED chrome border (.metallicBorder)
- REMOVED Box-level drawBehind rectangle (reflection overlay)
- REMOVED Row-level drawBehind rectangles (text background boxes)
- Only gradient background remains - no overlays or borders

**MetallicAccordion Complete Cleanup:**
- REMOVED chrome border (.metallicBorder)
- REMOVED Box-level drawBehind rectangle (reflection overlay)
- REMOVED background color from expanded section (square boxes)
- Clean gradient backgrounds only

**NeonText Simplification:**
- REMOVED 4-layer blur system (outer 18.dp, mid 12.dp, inner 6.dp blurs + sharp text)
- Single Text with subtle Shadow (8f blur radius, 30% alpha)
- No more visible halos or boxes around text
- Clean, crisp, professional appearance

**GlowingIcon Simplification:**
- REMOVED 4-layer blur system (12.dp, 8.dp, 4.dp blurs + sharp icon)
- Single clean Icon with no blur layers
- Professional appearance without excessive effects

**Dashboard Layout Improvements:**
- REMOVED ParticleStarfield background (too busy)
- REMOVED RadialGlowBackground (too busy)
- ADDED proper vertical scrolling to prevent cut-off
- Padding: 16.dp → 20.dp horizontal, 8.dp → 16.dp vertical (generous breathing room)
- Spacing: SpaceBetween → spacedBy(20.dp) for consistent gaps
- Hero title: headlineSmall → headlineMedium (more prominent)
- Hero button: 48.dp → 64.dp height, 0.85f → 0.9f width (more prominent)
- Icon size: 24.dp → 28.dp
- REMOVED CircularHUDProgress decoration (cleaner, simpler)

**Visual Result:**
- Professional app store quality appearance
- ZERO visible boxes or borders - completely borderless design
- No content cut-off - proper scrolling implemented
- Clean, modern, minimalist aesthetic
- Excellent spacing and breathing room
- Crisp, readable text without excessive blur
- Only gradient backgrounds for visual depth
- Accessible and production-ready for Samsung S23 FE (Android 14)