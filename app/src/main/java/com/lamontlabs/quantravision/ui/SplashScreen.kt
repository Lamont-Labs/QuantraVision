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
    
    // Trigger animations on composition
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000) // Show for 3 seconds
        onSplashComplete()
    }
    
    // Logo scale animation - zoom in effect
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    // Logo alpha animation - fade in effect
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    
    // Text alpha animation - delayed fade in
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "textAlpha"
    )
    
    // Glow pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Quantum Grid Background
        QuantumGridBackground(
            gridColor = NeonCyan.copy(alpha = 0.2f),
            animateGrid = true
        )
        
        // Radial glow from center
        RadialGlowBackground(
            glowColor = NeonCyan,
            centerAlpha = if (startAnimation) glowAlpha * 0.3f else 0f,
            edgeAlpha = 0f
        )
        
        // Content column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Metallic Hero Badge with Logo
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                MetallicHeroBadge(
                    pulseSync = startAnimation
                ) {
                    // Logo image (if exists, otherwise placeholder)
                    Image(
                        painter = painterResource(id = R.drawable.ic_qv_logo),
                        contentDescription = "QuantraVision Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // App name with neon glow
            NeonText(
                text = "QUANTRAVISION",
                modifier = Modifier.alpha(textAlpha),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                ),
                glowColor = NeonCyan,
                textColor = Color.White,
                glowIntensity = 0.9f,
                enablePulse = startAnimation
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "AI TRADING OVERLAY",
                modifier = Modifier.alpha(textAlpha),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = NeonCyanBright.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Loading indicator - subtle pulsing dots
            if (startAnimation) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.alpha(textAlpha)
                ) {
                    repeat(3) { index ->
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, delayMillis = index * 200),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot_$index"
                        )
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
