package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.core.PatternUsageLimiter

/**
 * SettingsScreen
 * - Theme, color, and font adjustments
 * - Displays app version, tier, and Lamont Labs attribution
 * - Fully local; no network calls
 */
@Composable
fun SettingsScreen(
    limiter: PatternUsageLimiter,
    currentTheme: MutableState<Boolean>,
    overlayAlpha: MutableState<Float>,
    fontScale: MutableState<Float>,
    onBack: () -> Unit
) {
    val tier = limiter.currentTier().name
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F15)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(20.dp)
                .background(Color(0xFF121820), RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings",
                color = Color(0xFF00E5FF),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))
            Divider(color = Color(0xFF1E2A33))
            Spacer(Modifier.height(16.dp))

            Text("Overlay Transparency", color = Color.White)
            Slider(
                value = overlayAlpha.value,
                onValueChange = { overlayAlpha.value = it },
                valueRange = 0.3f..1f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF))
            )

            Spacer(Modifier.height(16.dp))

            Text("Font Scale", color = Color.White)
            Slider(
                value = fontScale.value,
                onValueChange = { fontScale.value = it },
                valueRange = 0.8f..1.5f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF))
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", color = Color.White)
                Switch(
                    checked = currentTheme.value,
                    onCheckedChange = { currentTheme.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00E5FF),
                        checkedTrackColor = Color(0xFF005B73)
                    )
                )
            }

            Spacer(Modifier.height(20.dp))
            Divider(color = Color(0xFF1E2A33))
            Spacer(Modifier.height(20.dp))

            Text(
                "Current Tier: $tier",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "QuantraVision v1.2",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "© Lamont Labs",
                color = Color(0xFF4D6373),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("Back", color = Color.Black)
            }
        }
    }
}
