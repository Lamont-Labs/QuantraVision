package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// QuantraCore Brand Colors - Matching reference image aesthetic
val QVCyan = Color(0xFF00E5FF)           // Primary neon cyan (holographic glow)
val QVCyanBright = Color(0xFF00F0FF)     // Brighter cyan for emphasis & glows
val QVDarkBg = Color(0xFF0A1420)         // Deep navy background (QuantraCore exact match)
val QVSurface = Color(0xFF1A2332)        // Metallic surface (blue-tinted dark)
val QVLightText = Color(0xFFFFFFFF)      // Pure white text (maximum contrast)
val QVSuccess = Color(0xFF00FF88)        // Neon green (matches cyan aesthetic)
val QVOrange = Color(0xFFFF9800)         // Warm orange accent (QuantraCore highlight)
val QVError = Color(0xFFFF1744)          // Neon red (accessibility-safe)
val QVGold = Color(0xFFFFA726)           // Gold/bronze metallic (matches reference)
val QVInfo = Color(0xFF4DD0E1)           // Lighter cyan for secondary text

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
    
    // Background/Surface - Deep navy with blue tint (QuantraCore aesthetic)
    background = QVDarkBg,
    onBackground = QVLightText,
    surface = QVSurface,
    onSurface = QVLightText,
    surfaceVariant = Color(0xFF141F2E),   // Subtle blue tint for depth
    onSurfaceVariant = QVInfo,             // Lighter cyan for secondary text
    
    // Error (Neon red)
    error = QVError,
    onError = Color(0xFFFFFFFF),
    errorContainer = QVError.copy(alpha = 0.15f),
    onErrorContainer = QVError,
    
    // Outline/Border - Sharper, more visible with glow effect
    outline = QVCyan.copy(alpha = 0.6f),        // Increased from 0.3f for sharper borders
    outlineVariant = QVCyan.copy(alpha = 0.25f)  // Subtle glow for inactive borders
)

// Extension colors for semantic use (QuantraCore palette)
val ColorScheme.success: Color
    get() = QVSuccess

val ColorScheme.warning: Color
    get() = QVOrange

val ColorScheme.info: Color
    get() = QVInfo

val ColorScheme.gold: Color
    get() = QVGold

val ColorScheme.orange: Color
    get() = QVOrange

object QuantraColors {
    val cyanInt = 0xFF00E5FF.toInt()
    val cyanBrightInt = 0xFF00F0FF.toInt()
    val cyanBrightestInt = 0xFF00FFFF.toInt()
    val darkBgInt = 0xFF0A1420.toInt()
    val surfaceInt = 0xFF1A2332.toInt()
    val whiteInt = 0xFFFFFFFF.toInt()
    val successInt = 0xFF00FF88.toInt()
    val orangeInt = 0xFFFF9800.toInt()
    val errorInt = 0xFFFF1744.toInt()
    val goldInt = 0xFFFFA726.toInt()
    val infoInt = 0xFF4DD0E1.toInt()
    val outlineInt = 0xFF7A8A99.toInt()
}

@Composable
fun QuantraVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = QVColors, typography = Typography(), content = content)
}
