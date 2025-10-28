package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QVColors = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF001318),
    background = Color(0xFF0A1218),
    onBackground = Color(0xFFE6F7FF),
    surface = Color(0xFF14212C),
    onSurface = Color(0xFFE6F7FF),
    error = Color(0xFFFF5252)
)

@Composable
fun QuantraVisionTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = QVColors, typography = Typography(), content = content)
}
