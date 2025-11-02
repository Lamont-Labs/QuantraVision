package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// QuantraVision Brand Colors
val QVCyan = Color(0xFF00E5FF)           // Primary brand color (neon cyan)
val QVDarkBg = Color(0xFF0A1218)         // Dark background
val QVSurface = Color(0xFF14212C)        // Card/surface color
val QVLightText = Color(0xFFE6F7FF)      // Light text on dark
val QVSuccess = Color(0xFF4CAF50)        // Success/positive (green)
val QVWarning = Color(0xFFFF9800)        // Warning/neutral (orange)
val QVError = Color(0xFFF44336)          // Error/negative (red)
val QVGold = Color(0xFFFFD700)           // Achievement/premium (gold)
val QVInfo = Color(0xFF2196F3)           // Info/secondary (blue)

private val QVColors = darkColorScheme(
    // Primary colors (Cyan brand)
    primary = QVCyan,
    onPrimary = Color(0xFF001318),
    primaryContainer = QVCyan.copy(alpha = 0.2f),
    onPrimaryContainer = QVCyan,
    
    // Secondary colors (Blue)
    secondary = QVInfo,
    onSecondary = Color(0xFF001318),
    secondaryContainer = QVInfo.copy(alpha = 0.2f),
    onSecondaryContainer = QVInfo,
    
    // Tertiary colors (Gold for achievements)
    tertiary = QVGold,
    onTertiary = Color(0xFF001318),
    tertiaryContainer = QVGold.copy(alpha = 0.2f),
    onTertiaryContainer = QVGold,
    
    // Background/Surface
    background = QVDarkBg,
    onBackground = QVLightText,
    surface = QVSurface,
    onSurface = QVLightText,
    surfaceVariant = Color(0xFF1A2A3A),
    onSurfaceVariant = Color(0xFFB0BEC5),
    
    // Error
    error = QVError,
    onError = Color(0xFFFFFFFF),
    errorContainer = QVError.copy(alpha = 0.2f),
    onErrorContainer = QVError,
    
    // Outline/Border
    outline = QVCyan.copy(alpha = 0.3f),
    outlineVariant = Color(0xFF2A3A4A)
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
