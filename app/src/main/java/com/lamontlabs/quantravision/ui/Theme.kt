package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.R

// Premium Rajdhani font family - sharp, geometric, ultra-professional
val RajdhaniFont = FontFamily(
    Font(R.font.rajdhani_regular, FontWeight.Normal),
    Font(R.font.rajdhani_medium, FontWeight.Medium),
    Font(R.font.rajdhani_semibold, FontWeight.SemiBold),
    Font(R.font.rajdhani_bold, FontWeight.Bold)
)

// QUANTRACORE Color Palette - Premium Trading Terminal Aesthetic
// Exact colors from the reference image
val DeepNavyBackground = Color(0xFF0D1B2A)  // Darker, more premium background
val DarkSurface = Color(0xFF152232)          // Card/surface background
val MediumSurface = Color(0xFF1A2A3F)        // Elevated surfaces
val LightSurface = Color(0xFF243447)         // Highest elevation

// Cyan glow system - the signature QuantraVision look
val ElectricCyan = Color(0xFF00E5FF)         // Primary glowing cyan
val GlowCyan = Color(0xFF4DD0E1)             // Softer cyan glow
val SoftCyan = Color(0xFF80DEEA)             // Subtle cyan tint

// Metallic system - professional hardware aesthetic
val MetallicSilver = Color(0xFFB8C5D0)       // Main metallic color
val DarkSilver = Color(0xFF8A9BA8)           // Darker metallic accents
val Gunmetal = Color(0xFF5F6C7B)             // Deep metallic shadows
val Chrome = Color(0xFFDCE4EC)               // Bright metallic highlights

// Amber/Bronze accent system - subtle warm highlights
val AmberAccent = Color(0xFFFF9800)          // Warm rim lighting
val BronzeGlow = Color(0xFFFFA726)           // Softer bronze glow
val GoldHighlight = Color(0xFFFFD700)        // Premium gold accent

// Text system - ultra-crisp white text
val CrispWhite = Color(0xFFFAFDFF)           // Sharpest white for primary text
val TextWhite = Color(0xFFE6F7FF)            // Standard white text
val TextGray = Color(0xFF8A9BA8)             // Secondary text
val TextDim = Color(0xFF5F6C7B)              // Tertiary/disabled text

// Status colors - neon precision
val NeonGreen = Color(0xFF00FF88)            // Success/bullish
val NeonRed = Color(0xFFFF3B30)              // Error/bearish
val NeonOrange = Color(0xFFFF9500)           // Warning
val NeonBlue = Color(0xFF0A84FF)             // Info

private val QuantraColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepNavyBackground,
    primaryContainer = ElectricCyan.copy(alpha = 0.12f),
    onPrimaryContainer = ElectricCyan,
    
    secondary = MetallicSilver,
    onSecondary = DeepNavyBackground,
    secondaryContainer = MetallicSilver.copy(alpha = 0.08f),
    onSecondaryContainer = Chrome,
    
    tertiary = AmberAccent,
    onTertiary = DeepNavyBackground,
    tertiaryContainer = AmberAccent.copy(alpha = 0.10f),
    onTertiaryContainer = BronzeGlow,
    
    background = DeepNavyBackground,
    onBackground = CrispWhite,
    surface = DarkSurface,
    onSurface = CrispWhite,
    surfaceVariant = MediumSurface,
    onSurfaceVariant = TextGray,
    surfaceTint = ElectricCyan,
    
    error = NeonRed,
    onError = DeepNavyBackground,  // Dark text for high contrast on bright red error
    errorContainer = NeonRed.copy(alpha = 0.12f),
    onErrorContainer = CrispWhite,  // White text for high contrast on light error background
    
    outline = ElectricCyan.copy(alpha = 0.20f),
    outlineVariant = MediumSurface,
    scrim = Color(0xFF000000).copy(alpha = 0.6f),
    
    inverseSurface = MetallicSilver,
    inverseOnSurface = DeepNavyBackground,
    inversePrimary = Color(0xFF006B7D),
    
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = MediumSurface,
    surfaceContainerHighest = LightSurface,
    surfaceContainerLow = Color(0xFF0B1621),
    surfaceContainerLowest = Color(0xFF050A12)
)

// Extended color properties for premium UI
val ColorScheme.success: Color
    get() = NeonGreen

val ColorScheme.warning: Color
    get() = NeonOrange

val ColorScheme.info: Color
    get() = NeonBlue

val ColorScheme.gold: Color
    get() = GoldHighlight

val ColorScheme.metallic: Color
    get() = MetallicSilver

val ColorScheme.chrome: Color
    get() = Chrome

val ColorScheme.gunmetal: Color
    get() = Gunmetal

val ColorScheme.glowCyan: Color
    get() = GlowCyan

val ColorScheme.amber: Color
    get() = AmberAccent

val ColorScheme.textSecondary: Color
    get() = TextGray

val ColorScheme.textDim: Color
    get() = TextDim

// Premium glow effect for primary text (simulates the cyan glow in the image)
val CyanGlowShadow = Shadow(
    color = ElectricCyan.copy(alpha = 0.6f),
    offset = Offset(0f, 0f),
    blurRadius = 12f
)

val SubtleGlowShadow = Shadow(
    color = ElectricCyan.copy(alpha = 0.3f),
    offset = Offset(0f, 0f),
    blurRadius = 8f
)

// QUANTRACORE Typography System
// Sharp, geometric, ultra-crisp Rajdhani font matching the reference image
private val QuantraTypography = Typography(
    // Display styles - for hero titles and major headings
    displayLarge = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.5).sp,  // Tighter tracking for impact
        color = CrispWhite,
        shadow = CyanGlowShadow
    ),
    displayMedium = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
        color = CrispWhite,
        shadow = CyanGlowShadow
    ),
    displaySmall = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        color = CrispWhite,
        shadow = SubtleGlowShadow
    ),
    
    // Headline styles - for section headers
    headlineLarge = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = CrispWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = CrispWhite
    ),
    headlineSmall = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = TextWhite
    ),
    
    // Title styles - for card titles and important labels
    titleLarge = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = TextWhite
    ),
    titleMedium = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = TextWhite
    ),
    titleSmall = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = TextWhite
    ),
    
    // Body styles - for readable content
    bodyLarge = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = TextWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = TextWhite
    ),
    bodySmall = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = TextGray
    ),
    
    // Label styles - for UI controls and micro-copy
    labelLarge = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = TextWhite
    ),
    labelMedium = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextGray
    ),
    labelSmall = TextStyle(
        fontFamily = RajdhaniFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextGray
    )
)

@Composable
fun QuantraVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QuantraColorScheme,
        typography = QuantraTypography,
        content = content
    )
}
