package com.lamontlabs.quantravision.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val QDarkScheme = darkColorScheme(
  primary = Color(0xFF00D1FF),
  secondary = Color(0xFF2CC3F8),
  background = Color(0xFF071423),
  surface = Color(0xFF0B1C2E),
  onPrimary = Color(0xFF001219),
  onBackground = Color(0xFFD8F5FF),
  onSurface = Color(0xFFD8F5FF),
  outline = Color(0xFF14405C)
)

@Composable
fun QuantraVisionTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = QDarkScheme,
    typography = Typography(), // picks font from theme xml
    content = content
  )
}
