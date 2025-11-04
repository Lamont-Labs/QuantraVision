package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val DeepNavy = Color(0xFF0A1628)
val DarkSurface = Color(0xFF14212C)
val MediumSurface = Color(0xFF1A2A3A)
val LightSurface = Color(0xFF243447)

val ElectricCyan = Color(0xFF00E5FF)
val GlowCyan = Color(0xFF4DD0E1)
val SoftCyan = Color(0xFF80DEEA)

val MetallicSilver = Color(0xFFCFD8DC)
val DarkSilver = Color(0xFFB0BEC5)
val TextWhite = Color(0xFFE6F7FF)
val TextGray = Color(0xFF90A4AE)

val NeonGreen = Color(0xFF00FF88)
val NeonOrange = Color(0xFFFF9500)
val NeonRed = Color(0xFFFF3B30)
val NeonGold = Color(0xFFFFD700)

private val QuantraColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepNavy,
    primaryContainer = ElectricCyan.copy(alpha = 0.15f),
    onPrimaryContainer = GlowCyan,
    
    secondary = GlowCyan,
    onSecondary = DeepNavy,
    secondaryContainer = GlowCyan.copy(alpha = 0.12f),
    onSecondaryContainer = SoftCyan,
    
    tertiary = NeonGold,
    onTertiary = DeepNavy,
    tertiaryContainer = NeonGold.copy(alpha = 0.12f),
    onTertiaryContainer = NeonGold,
    
    background = DeepNavy,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = MediumSurface,
    onSurfaceVariant = DarkSilver,
    surfaceTint = ElectricCyan,
    
    error = NeonRed,
    onError = TextWhite,
    errorContainer = NeonRed.copy(alpha = 0.15f),
    onErrorContainer = NeonRed,
    
    outline = ElectricCyan.copy(alpha = 0.25f),
    outlineVariant = MediumSurface,
    scrim = Color(0xFF000000).copy(alpha = 0.5f),
    
    inverseSurface = MetallicSilver,
    inverseOnSurface = DeepNavy,
    inversePrimary = Color(0xFF006B7D),
    
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = MediumSurface,
    surfaceContainerHighest = LightSurface,
    surfaceContainerLow = Color(0xFF0D1923),
    surfaceContainerLowest = Color(0xFF050B12)
)

val ColorScheme.success: Color
    get() = NeonGreen

val ColorScheme.warning: Color
    get() = NeonOrange

val ColorScheme.info: Color
    get() = GlowCyan

val ColorScheme.gold: Color
    get() = NeonGold

val ColorScheme.metallic: Color
    get() = MetallicSilver

val ColorScheme.glowCyan: Color
    get() = GlowCyan

val ColorScheme.textSecondary: Color
    get() = TextGray

private val QuantraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        color = TextWhite
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
        color = TextWhite
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        color = TextWhite
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = TextWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = TextWhite
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = TextWhite
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = TextWhite
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = TextWhite
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = TextWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = TextWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = TextWhite
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = TextGray
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = TextWhite
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextGray
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
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
