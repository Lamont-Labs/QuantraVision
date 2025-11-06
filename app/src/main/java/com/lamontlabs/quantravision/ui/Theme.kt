package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// QuantraCore HD Contrast Colors - Sharp, High-Definition UI
val QVCyan = Color(0xFF00F0FF)           // BRIGHTER primary neon cyan (HD contrast)
val QVCyanBright = Color(0xFF00FFFF)     // BRIGHTEST cyan for sharp edges
val QVDarkBg = Color(0xFF05070D)         // DEEPER background (near-black for max contrast)
val QVSurface = Color(0xFF0D1219)        // DARKER surface (stronger separation)
val QVLightText = Color(0xFFFFFFFF)      // Pure white text (maximum contrast)
val QVSuccess = Color(0xFF00FF88)        // Bright neon green
val QVOrange = Color(0xFFFF9F00)         // BRIGHTER orange accent
val QVError = Color(0xFFFF1F50)          // BRIGHTER neon red
val QVGold = Color(0xFFFFB347)           // BRIGHTER gold metallic
val QVInfo = Color(0xFF5FDDEB)           // BRIGHTER cyan for secondary text

private val QVColors = darkColorScheme(
    // Primary colors (BRIGHT Neon Cyan - HD contrast)
    primary = QVCyan,
    onPrimary = Color(0xFF000000),
    primaryContainer = QVCyan.copy(alpha = 0.25f),  // HIGHER alpha for visibility
    onPrimaryContainer = QVCyanBright,
    
    // Secondary colors (BRIGHT Cyan)
    secondary = QVInfo,
    onSecondary = Color(0xFF000000),
    secondaryContainer = QVInfo.copy(alpha = 0.25f),  // HIGHER alpha
    onSecondaryContainer = QVInfo,
    
    // Tertiary colors (BRIGHT Gold)
    tertiary = QVGold,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = QVGold.copy(alpha = 0.25f),  // HIGHER alpha
    onTertiaryContainer = QVGold,
    
    // Background/Surface - DEEPEST black with blue tint (MAX contrast)
    background = QVDarkBg,
    onBackground = QVLightText,
    surface = QVSurface,
    onSurface = QVLightText,
    surfaceVariant = Color(0xFF0A0E14),   // DARKER variant for depth
    onSurfaceVariant = QVCyanBright,      // BRIGHTER secondary text
    
    // Error (BRIGHT Neon red)
    error = QVError,
    onError = Color(0xFFFFFFFF),
    errorContainer = QVError.copy(alpha = 0.25f),  // HIGHER alpha
    onErrorContainer = QVError,
    
    // Outline/Border - SHARPER, more SOLID borders
    outline = QVCyan.copy(alpha = 0.85f),       // MUCH HIGHER for crisp edges
    outlineVariant = QVCyan.copy(alpha = 0.45f)  // Still visible when inactive
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
    val cyanInt = 0xFF00F0FF.toInt()         // BRIGHTER primary
    val cyanBrightInt = 0xFF00FFFF.toInt()   // BRIGHTEST for edges
    val cyanBrightestInt = 0xFF00FFFF.toInt()
    val darkBgInt = 0xFF05070D.toInt()       // DEEPER background
    val surfaceInt = 0xFF0D1219.toInt()      // DARKER surface
    val whiteInt = 0xFFFFFFFF.toInt()
    val successInt = 0xFF00FF88.toInt()
    val orangeInt = 0xFFFF9F00.toInt()       // BRIGHTER orange
    val errorInt = 0xFFFF1F50.toInt()        // BRIGHTER red
    val goldInt = 0xFFFFB347.toInt()         // BRIGHTER gold
    val infoInt = 0xFF5FDDEB.toInt()         // BRIGHTER info cyan
    val outlineInt = 0xFFAABBCC.toInt()      // LIGHTER outline
}

@Composable
fun QuantraVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = QVColors, typography = Typography(), content = content)
}
