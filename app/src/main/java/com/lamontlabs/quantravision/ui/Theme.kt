package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// QuantraVision Brand Colors - Sharpened for high contrast
val QVCyan = Color(0xFF00E5FF)           // Primary neon cyan (matches icon)
val QVCyanBright = Color(0xFF00FFFF)     // Brighter cyan for emphasis
val QVDarkBg = Color(0xFF0A1218)         // Dark blue-black background (balanced contrast)
val QVSurface = Color(0xFF14212C)        // Card/surface (sharper contrast from bg)
val QVLightText = Color(0xFFFFFFFF)      // Pure white text (maximum contrast)
val QVSuccess = Color(0xFF00FF88)        // Neon green (matches cyan aesthetic)
val QVWarning = Color(0xFFFFAA00)        // Neon orange
val QVError = Color(0xFFFF1744)          // Neon red (accessibility-safe)
val QVGold = Color(0xFFFFD700)           // Achievement/premium (gold)
val QVInfo = Color(0xFF0099FF)           // Neon blue

private val QVColors = darkColorScheme(
    // Primary colors (Neon Cyan - matches icon)
    primary = QVCyan,
    onPrimary = Color(0xFF000000),
    primaryContainer = QVCyan.copy(alpha = 0.15f),
    onPrimaryContainer = QVCyanBright,
    
    // Secondary colors (Neon Blue)
    secondary = QVInfo,
    onSecondary = Color(0xFF000000),
    secondaryContainer = QVInfo.copy(alpha = 0.15f),
    onSecondaryContainer = QVInfo,
    
    // Tertiary colors (Gold for achievements)
    tertiary = QVGold,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = QVGold.copy(alpha = 0.15f),
    onTertiaryContainer = QVGold,
    
    // Background/Surface - Deeper blacks, sharper contrast
    background = QVDarkBg,
    onBackground = QVLightText,
    surface = QVSurface,
    onSurface = QVLightText,
    surfaceVariant = Color(0xFF0F1D28),
    onSurfaceVariant = Color(0xFFE0E0E0),
    
    // Error (Neon red)
    error = QVError,
    onError = Color(0xFFFFFFFF),
    errorContainer = QVError.copy(alpha = 0.15f),
    onErrorContainer = QVError,
    
    // Outline/Border - Sharper, more visible with glow effect
    outline = QVCyan.copy(alpha = 0.6f),        // Increased from 0.3f for sharper borders
    outlineVariant = QVCyan.copy(alpha = 0.25f)  // Subtle glow for inactive borders
)

// Extension colors for semantic use
val ColorScheme.success: Color
    get() = QVSuccess

val ColorScheme.warning: Color
    get() = QVWarning

val ColorScheme.info: Color
    get() = QVInfo

val ColorScheme.gold: Color
    get() = QVGold

@Composable
fun QuantraVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = QVColors, typography = Typography(), content = content)
}
