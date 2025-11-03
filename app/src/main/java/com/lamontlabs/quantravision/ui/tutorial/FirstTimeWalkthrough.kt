package com.lamontlabs.quantravision.ui.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.ui.success
import com.lamontlabs.quantravision.ui.warning
import com.lamontlabs.quantravision.ui.gold

/**
 * FirstTimeWalkthrough - Interactive in-app tutorial
 * 
 * Shows users the CORRECT sequence:
 * 1. Open your chart app FIRST (TradingView, Webull, etc.)
 * 2. Open QuantraVision
 * 3. Tap "Start Overlay"
 * 4. Watch patterns appear
 */
@Composable
fun FirstTimeWalkthrough(
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    
    val steps = listOf(
        WalkthroughStep(
            title = "Welcome to QuantraVision!",
            description = "The most advanced offline AI pattern detection for retail traders.\n\nLet's get you started in 4 simple steps.",
            icon = Icons.Default.Star,
            iconColor = MaterialTheme.colorScheme.primary,
            showOrderBadge = false
        ),
        WalkthroughStep(
            title = "Step 1: Open Your Chart App FIRST",
            description = "Before opening QuantraVision, open your trading chart:\n\n• TradingView\n• Webull\n• Robinhood\n• MetaTrader\n• Any chart app\n\nDisplay the chart you want to analyze.",
            icon = Icons.Default.ShowChart,
            iconColor = MaterialTheme.colorScheme.success,
            showOrderBadge = true,
            orderNumber = 1,
            emphasized = "FIRST"
        ),
        WalkthroughStep(
            title = "Step 2: Open QuantraVision",
            description = "Now that your chart is visible on screen:\n\n1. Press Home button (chart stays in background)\n2. Open QuantraVision app\n3. You'll see the dashboard",
            icon = Icons.Default.Apps,
            iconColor = MaterialTheme.colorScheme.primary,
            showOrderBadge = true,
            orderNumber = 2
        ),
        WalkthroughStep(
            title = "Step 3: Start Overlay",
            description = "From the QuantraVision dashboard:\n\n1. Tap 'Start Overlay' button\n2. Grant overlay permission if prompted\n3. QuantraVision will appear on top of your chart\n4. Wait 5-10 seconds for AI detection",
            icon = Icons.Default.Layers,
            iconColor = MaterialTheme.colorScheme.warning,
            showOrderBadge = true,
            orderNumber = 3
        ),
        WalkthroughStep(
            title = "Step 4: View Detections!",
            description = "You'll see patterns highlighted on your chart:\n\n🟢 Green = Bullish patterns\n🔴 Red = Bearish patterns\n🔵 Blue = Forming patterns\n\nTap any pattern to learn more!\n\nEnable voice announcements in Settings for hands-free alerts.",
            icon = Icons.Default.CheckCircle,
            iconColor = MaterialTheme.colorScheme.success,
            showOrderBadge = true,
            orderNumber = 4
        ),
        WalkthroughStep(
            title = "You're All Set!",
            description = "Remember the sequence:\n\n1️⃣ Chart app FIRST\n2️⃣ QuantraVision second\n3️⃣ Start Overlay\n4️⃣ Watch the magic happen!\n\nTip: You can replay this tutorial anytime from Settings → Help → Tutorial",
            icon = Icons.Default.Celebration,
            iconColor = MaterialTheme.colorScheme.gold,
            showOrderBadge = false
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentStep) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index <= currentStep) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                    if (index < steps.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Step content
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                StepContent(step = steps[currentStep])
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Skip/Back button
                TextButton(
                    onClick = {
                        if (currentStep == 0) {
                            onSkip()
                        } else {
                            currentStep--
                        }
                    }
                ) {
                    Text(
                        text = if (currentStep == 0) "Skip Tutorial" else "Back",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Next/Done button
                Button(
                    onClick = {
                        if (currentStep < steps.size - 1) {
                            currentStep++
                        } else {
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentStep < steps.size - 1) "Next" else "Get Started!",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (currentStep < steps.size - 1) 
                            Icons.Default.ArrowForward 
                        else 
                            Icons.Default.Check,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun StepContent(step: WalkthroughStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with order badge
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = step.iconColor
            )
            
            if (step.showOrderBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-10).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.orderNumber.toString(),
                        color = MaterialTheme.colorScheme.background,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = step.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = step.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        // Emphasized text if present
        if (step.emphasized != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "⚠️ Important: Open chart app ${step.emphasized}",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Walkthrough step data
 */
private data class WalkthroughStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val showOrderBadge: Boolean = false,
    val orderNumber: Int = 0,
    val emphasized: String? = null
)
