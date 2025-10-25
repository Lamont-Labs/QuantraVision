package com.lamontlabs.quantravision.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.core.PatternUsageLimiter

/**
 * UsageOverlay
 * - Displays usage remaining (for FREE tier)
 * - Automatically disappears when user upgrades
 * - Uses translucent background to avoid blocking charts
 */
@Composable
fun UsageOverlay(limiter: PatternUsageLimiter) {
    val state by remember { mutableStateOf(limiter.state()) }
    AnimatedVisibility(visible = state.tier == PatternUsageLimiter.Tier.FREE && state.detectionsRemaining <= 5) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xAA000000))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Free Tier: ${state.detectionsRemaining} detections left",
                    color = Color(0xFF00E5FF),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (state.detectionsRemaining == 0) {
                    Text(
                        text = "Upgrade to continue detecting patterns.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
