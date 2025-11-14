package com.lamontlabs.quantravision.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.OrbitronFontFamily
import com.lamontlabs.quantravision.ui.SpaceGroteskFontFamily
import com.lamontlabs.quantravision.ui.StaticBrandBackground

/**
 * # QuantraVision Design System
 * 
 * Comprehensive Material Design 3 design system that consolidates all design tokens,
 * typography, spacing, colors, and reusable components for QuantraVision.
 * 
 * ## Design Philosophy
 * - **Material Design 3**: Modern, accessible, and consistent UI patterns
 * - **Dual Font System**: Orbitron for headers (futuristic branding), Space Grotesk for body (readability)
 * - **Metallic/Neon Aesthetic**: Premium chrome surfaces with neon cyan accents
 * - **High Contrast**: WCAG-compliant color ratios for accessibility
 * - **No Animations**: Clean, professional UI without distracting motion
 * 
 * ## Usage
 * ```kotlin
 * // Access spacing tokens
 * Box(modifier = Modifier.padding(AppSpacing.lg))
 * 
 * // Use typography styles
 * Text(text = "Header", style = MaterialTheme.typography.headlineLarge)
 * 
 * // Apply color palette
 * Box(modifier = Modifier.background(AppColors.NeonCyan))
 * 
 * // Use components
 * MetallicText(text = "Premium Text", enableGlow = true)
 * MetallicCard { /* content */ }
 * ```
 */

// ============================================================================
// SPACING SYSTEM
// ============================================================================

/**
 * Spacing tokens for consistent layout and padding throughout the app.
 * 
 * ## Usage
 * Use these tokens instead of hardcoded dp values for consistent spacing:
 * ```kotlin
 * Column(
 *     modifier = Modifier.padding(AppSpacing.base),
 *     verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
 * ) { /* content */ }
 * ```
 * 
 * ## Scale
 * - `xxs` (2dp): Minimal spacing, tight layouts
 * - `xs` (4dp): Very small spacing, icon padding
 * - `sm` (8dp): Small spacing, compact UI elements
 * - `md` (12dp): Medium spacing, list item padding
 * - `base` (16dp): Default spacing, card padding
 * - `lg` (24dp): Large spacing, section separation
 * - `xl` (32dp): Extra large spacing, major sections
 * - `xxl` (48dp): Extra extra large spacing, hero sections
 * - `xxxl` (64dp): Maximum spacing, splash screens
 */
object AppSpacing {
    /** 2dp - Minimal spacing, tight layouts */
    val xxs: Dp = 2.dp
    
    /** 4dp - Very small spacing, icon padding */
    val xs: Dp = 4.dp
    
    /** 8dp - Small spacing, compact UI elements */
    val sm: Dp = 8.dp
    
    /** 12dp - Medium spacing, list item padding */
    val md: Dp = 12.dp
    
    /** 16dp - Default spacing, card padding (recommended baseline) */
    val base: Dp = 16.dp
    
    /** 24dp - Large spacing, section separation */
    val lg: Dp = 24.dp
    
    /** 32dp - Extra large spacing, major sections */
    val xl: Dp = 32.dp
    
    /** 48dp - Extra extra large spacing, hero sections */
    val xxl: Dp = 48.dp
    
    /** 64dp - Maximum spacing, splash screens */
    val xxxl: Dp = 64.dp
}

// ============================================================================
// TYPOGRAPHY SYSTEM
// ============================================================================

/**
 * Complete Material Design 3 typography scale for QuantraVision.
 * 
 * ## Font Families
 * - **Orbitron**: Display, Headline, Title styles (futuristic, matches logo)
 * - **Space Grotesk**: Body, Label styles (clean, readable)
 * 
 * ## Usage
 * ```kotlin
 * // Use via MaterialTheme
 * Text(
 *     text = "Section Header",
 *     style = MaterialTheme.typography.headlineLarge
 * )
 * 
 * // Or reference directly
 * Text(text = "Body Text", style = AppTypography.bodyLarge)
 * ```
 * 
 * ## Scale Categories
 * - **Display**: Extra large headers (57sp - 36sp)
 * - **Headline**: Section headers (32sp - 24sp)
 * - **Title**: Card titles, important labels (22sp - 14sp)
 * - **Body**: Regular content (16sp - 12sp)
 * - **Label**: Buttons, small UI text (14sp - 11sp)
 */
val AppTypography = Typography(
    // ========================================================================
    // DISPLAY - Extra Large Headers
    // ========================================================================
    
    /**
     * Display Large - 57sp
     * Use for: Hero headlines, splash screen titles, app name
     */
    displayLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.25).sp
    ),
    
    /**
     * Display Medium - 45sp
     * Use for: Large screen headers, marketing headlines
     */
    displayMedium = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    
    /**
     * Display Small - 36sp
     * Use for: Page headers, primary screen titles
     */
    displaySmall = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    
    // ========================================================================
    // HEADLINE - Section Headers
    // ========================================================================
    
    /**
     * Headline Large - 32sp
     * Use for: Major section headers, feature titles
     */
    headlineLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    
    /**
     * Headline Medium - 28sp
     * Use for: Section headers, dialog titles
     */
    headlineMedium = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    
    /**
     * Headline Small - 24sp
     * Use for: Subsection headers, card group titles
     */
    headlineSmall = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    
    // ========================================================================
    // TITLE - Card Titles, Important Labels
    // ========================================================================
    
    /**
     * Title Large - 22sp
     * Use for: Card titles, list item headers
     */
    titleLarge = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    
    /**
     * Title Medium - 16sp
     * Use for: Tab labels, small card titles
     */
    titleMedium = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp
    ),
    
    /**
     * Title Small - 14sp
     * Use for: Compact headers, dense list titles
     */
    titleSmall = TextStyle(
        fontFamily = OrbitronFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    
    // ========================================================================
    // BODY - Regular Content
    // ========================================================================
    
    /**
     * Body Large - 16sp (Default for paragraphs)
     * Use for: Main body text, descriptions, paragraphs
     */
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    
    /**
     * Body Medium - 14sp
     * Use for: Secondary text, list item descriptions
     */
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    ),
    
    /**
     * Body Small - 12sp
     * Use for: Captions, helper text, metadata
     */
    bodySmall = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp
    ),
    
    // ========================================================================
    // LABEL - Buttons, Small Text
    // ========================================================================
    
    /**
     * Label Large - 14sp
     * Use for: Button text, prominent labels
     */
    labelLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    
    /**
     * Label Medium - 12sp
     * Use for: Chip labels, small buttons
     */
    labelMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    ),
    
    /**
     * Label Small - 11sp
     * Use for: Tiny labels, overline text
     */
    labelSmall = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)

// ============================================================================
// COLOR PALETTE
// ============================================================================

/**
 * Consolidated color palette for QuantraVision.
 * 
 * ## Color Categories
 * - **Base**: Background, surface, text colors
 * - **Metallic**: Chrome, silver, gold metallic accents
 * - **Neon**: Bright cyan, gold, purple glow colors
 * - **Tier Colors**: Subscription tier visual identifiers
 * - **Status**: Success, warning, error, info states
 * 
 * ## Usage
 * ```kotlin
 * // Background
 * Box(modifier = Modifier.background(AppColors.Background))
 * 
 * // Metallic text
 * Text(text = "Premium", color = AppColors.MetallicChrome)
 * 
 * // Neon accent
 * Icon(tint = AppColors.NeonCyan)
 * 
 * // Tier badge
 * Badge(backgroundColor = AppColors.TierPro)
 * ```
 */
object AppColors {
    // ========================================================================
    // BASE COLORS
    // ========================================================================
    
    /** Pure black background (#000000) - Primary app background */
    val Background = Color.Black
    
    /** Dark gray surface (#1A1A1A) - Cards, dialogs, elevated surfaces */
    val Surface = Color(0xFF1A1A1A)
    
    /** Pure white text (#FFFFFF) - Primary text on dark backgrounds */
    val OnBackground = Color.White
    
    // ========================================================================
    // METALLIC COLORS
    // ========================================================================
    
    /** Metallic silver (#C0C0C0) - Chrome text, premium accents */
    val MetallicSilver = Color(0xFFC0C0C0)
    
    /** Metallic gold (#FFB347) - Gold tier, premium features */
    val MetallicGold = Color(0xFFFFB347)
    
    /** Metallic chrome (#E8E8E8) - Bright chrome highlights, reflections */
    val MetallicChrome = Color(0xFFE8E8E8)
    
    // ========================================================================
    // NEON COLORS
    // ========================================================================
    
    /** Neon cyan (#00F0FF) - Primary brand color, interactive elements */
    val NeonCyan = Color(0xFF00F0FF)
    
    /** Neon cyan bright (#5FDDEB) - Lighter cyan, highlights, hover states */
    val NeonCyanBright = Color(0xFF5FDDEB)
    
    /** Neon gold (#FFB347) - Secondary accent, premium features */
    val NeonGold = Color(0xFFFFB347)
    
    /** Neon purple (#BB86FC) - Tertiary accent, Pro tier */
    val NeonPurple = Color(0xFFBB86FC)
    
    // ========================================================================
    // TIER COLORS - Subscription Visual Identifiers
    // ========================================================================
    
    /** Free tier color (#9E9E9E) - Gray for free/limited features */
    val TierFree = Color(0xFF9E9E9E)
    
    /** Starter tier color (#00F0FF) - Cyan for starter plan */
    val TierStarter = Color(0xFF00F0FF)
    
    /** Standard tier color (#FFB347) - Gold for standard plan */
    val TierStandard = Color(0xFFFFB347)
    
    /** Pro tier color (#BB86FC) - Purple for professional plan */
    val TierPro = Color(0xFFBB86FC)
    
    // ========================================================================
    // STATUS COLORS
    // ========================================================================
    
    /** Success green (#4CAF50) - Positive actions, confirmations */
    val Success = Color(0xFF4CAF50)
    
    /** Warning orange (#FF9800) - Cautions, important notices */
    val Warning = Color(0xFFFF9800)
    
    /** Error red (#F44336) - Errors, destructive actions */
    val Error = Color(0xFFF44336)
    
    /** Info blue (#2196F3) - Informational messages, help text */
    val Info = Color(0xFF2196F3)
}

// ============================================================================
// ELEVATION LEVELS
// ============================================================================

/**
 * Elevation levels for shadow depth and visual hierarchy.
 * 
 * ## Material Design 3 Elevation
 * Elevation creates depth through shadows and layers. Use consistently across the app.
 * 
 * ## Usage
 * ```kotlin
 * Card(
 *     elevation = CardDefaults.cardElevation(
 *         defaultElevation = AppElevation.medium
 *     )
 * )
 * ```
 * 
 * ## Levels
 * - `none` (0dp): Flat surfaces, no depth
 * - `low` (2dp): Subtle depth, floating elements
 * - `medium` (4dp): Standard cards, buttons
 * - `high` (8dp): Dialogs, menus, modals
 * - `highest` (16dp): Navigation drawers, app bars
 */
object AppElevation {
    /** 0dp - No elevation, flat on surface */
    val none: Dp = 0.dp
    
    /** 2dp - Low elevation, subtle floating */
    val low: Dp = 2.dp
    
    /** 4dp - Medium elevation, standard cards */
    val medium: Dp = 4.dp
    
    /** 8dp - High elevation, dialogs and menus */
    val high: Dp = 8.dp
    
    /** 16dp - Highest elevation, navigation elements */
    val highest: Dp = 16.dp
}

// ============================================================================
// BORDER RADIUS
// ============================================================================

/**
 * Border radius tokens for consistent rounded corners.
 * 
 * ## Usage
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .clip(RoundedCornerShape(AppRadius.md))
 *         .background(color)
 * )
 * ```
 * 
 * ## Scale
 * - `xs` (4dp): Minimal rounding, subtle corners
 * - `sm` (8dp): Small rounding, compact elements
 * - `md` (12dp): Medium rounding, cards, buttons
 * - `lg` (16dp): Large rounding, prominent cards
 * - `xl` (24dp): Extra large rounding, hero elements
 * - `circle` (9999dp): Perfect circle/pill shape
 */
object AppRadius {
    /** 4dp - Extra small radius, minimal rounding */
    val xs: Dp = 4.dp
    
    /** 8dp - Small radius, compact UI elements */
    val sm: Dp = 8.dp
    
    /** 12dp - Medium radius, standard cards and buttons */
    val md: Dp = 12.dp
    
    /** 16dp - Large radius, prominent cards */
    val lg: Dp = 16.dp
    
    /** 24dp - Extra large radius, hero elements */
    val xl: Dp = 24.dp
    
    /** 9999dp - Perfect circle/pill shape */
    val circle: Dp = 9999.dp
}

// ============================================================================
// COMPONENT LIBRARY - Consolidated References
// ============================================================================

/**
 * MetallicText - Convenience wrapper for text with optional neon glow.
 * 
 * ## Description
 * Combines regular Text and NeonText components into a single API with a glow toggle.
 * Use this for consistent metallic/chrome text styling throughout the app.
 * 
 * ## Usage
 * ```kotlin
 * // Standard metallic text
 * MetallicText(
 *     text = "Premium Feature",
 *     style = MaterialTheme.typography.titleLarge
 * )
 * 
 * // With neon glow effect
 * MetallicText(
 *     text = "QuantraVision",
 *     style = MaterialTheme.typography.displayLarge,
 *     enableGlow = true
 * )
 * ```
 * 
 * ## When to Use
 * - **Standard (no glow)**: Card titles, labels, general UI text
 * - **With glow**: Hero headlines, app branding, feature highlights
 * 
 * @param text Text content to display
 * @param modifier Modifier for customization
 * @param style Text style from typography scale (default: bodyLarge)
 * @param color Text color (default: chrome metallic)
 * @param enableGlow Enable neon glow effect (default: false)
 */
@Composable
fun MetallicText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = AppColors.MetallicChrome,
    enableGlow: Boolean = false
) {
    if (enableGlow) {
        NeonText(
            text = text,
            modifier = modifier,
            style = style,
            glowColor = AppColors.NeonCyan,
            textColor = color
        )
    } else {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = color
        )
    }
}

// ============================================================================
// COMPONENT REFERENCES
// ============================================================================

/**
 * ## Available Components from Design System
 * 
 * These components are available from the existing codebase.
 * Import and use them directly:
 * 
 * ### Cards
 * ```kotlin
 * import com.lamontlabs.quantravision.ui.MetallicCard
 * 
 * MetallicCard(
 *     onClick = { /* action */ },
 *     enableShimmer = false,
 *     elevation = AppElevation.medium
 * ) {
 *     // Card content
 * }
 * ```
 * 
 * ### Neon Text
 * ```kotlin
 * import com.lamontlabs.quantravision.ui.NeonText
 * 
 * NeonText(
 *     text = "Glowing Text",
 *     glowColor = AppColors.NeonCyan,
 *     textColor = AppColors.MetallicChrome
 * )
 * ```
 * 
 * ### Background
 * ```kotlin
 * import com.lamontlabs.quantravision.ui.StaticBrandBackground
 * 
 * StaticBrandBackground(modifier = Modifier.fillMaxSize())
 * ```
 * 
 * ## Design Guidelines
 * 
 * ### Typography Hierarchy
 * - **Display**: Hero sections, splash screens (rarely used)
 * - **Headline**: Page headers, major sections
 * - **Title**: Card headers, dialog titles
 * - **Body**: Paragraphs, descriptions, main content
 * - **Label**: Buttons, chips, small UI elements
 * 
 * ### Color Usage
 * - **Background/Surface**: Dark theme with high contrast
 * - **Metallic**: Premium accents, chrome highlights
 * - **Neon**: Interactive elements, brand colors
 * - **Tier Colors**: Subscription plan visual identifiers
 * - **Status**: Feedback, alerts, confirmations
 * 
 * ### Spacing
 * - Prefer `base` (16dp) as default padding
 * - Use `lg` (24dp) between major sections
 * - Use `sm` (8dp) between related elements
 * - Use `xl` (32dp) for hero/featured content
 * 
 * ### Elevation
 * - Cards: `medium` (4dp)
 * - Floating buttons: `low` (2dp)
 * - Dialogs/modals: `high` (8dp)
 * - Navigation: `highest` (16dp)
 */
