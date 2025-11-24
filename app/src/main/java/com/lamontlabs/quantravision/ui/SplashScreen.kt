package com.lamontlabs.quantravision.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.R
import kotlinx.coroutines.delay

/**
 * Animated Splash Screen with Metallic Logo Reveal
 * Creates cinematic app launch experience
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Trigger splash complete after delay
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000) // Show for 3 seconds
        onSplashComplete()
    }
    
    // DISABLED: No animations per user requirement - static values
    val logoScale = 1f
    val logoAlpha = 1f
    val textAlpha = 1f
    val glowAlpha = 0.6f
    
    StaticBrandBackground(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Content column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // QuantraVision Hero Logo (full branding with text)
            Image(
                painter = painterResource(id = R.drawable.quantravision_hero_logo),
                contentDescription = "QuantraVision AI Trading Overlay",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Loading indicator - static dots (no animations)
            if (startAnimation) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.alpha(textAlpha)
                ) {
                    repeat(3) { index ->
                        // DISABLED: No animations per user requirement - static dot alpha
                        val dotAlpha = 0.7f
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .alpha(dotAlpha)
                                .background(NeonCyan, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
    }
}
