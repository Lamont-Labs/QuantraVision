package com.lamontlabs.quantravision.ui.screens.scan

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.*

/**
 * Scan Screen - Immersive Detection Interface
 * 
 * Features maximum visual detail with:
 * - Layered quantum grid background
 * - Neon text and bounding box effects
 * - Glass-morphic cards for steps
 * - Metallic feature cards
 * - Animated pulse indicators
 * - Holographic scan preview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    context: Context,
    onStartScan: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Static brand background - no animations
        StaticBrandBackground(modifier = Modifier.fillMaxSize())
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        NeonText(
                            text = "Pattern Detection",
                            style = MaterialTheme.typography.headlineSmall,
                            glowColor = NeonCyan,
                            enablePulse = false
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A0E1A).copy(alpha = 0.85f)
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hero Section with Animated Detection Preview
                item {
                    HeroSection()
                }
                
                // Animated NeonBoundingBox Preview
                item {
                    ScanPreviewCard()
                }
                
                // Status Indicator - Ready to Scan
                item {
                    StatusIndicator()
                }
                
                // Main CTA - Start Detection Button
                item {
                    MetallicButton(
                        onClick = onStartScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        showTopStrip = true
                    ) {
                        GlowingIcon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            size = 32.dp,
                            glowColor = NeonCyan,
                            glowIntensity = 0.9f
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "START DETECTION",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                
                // Section Title: How It Works
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    NeonText(
                        text = "How It Works",
                        style = MaterialTheme.typography.headlineMedium,
                        glowColor = NeonCyan,
                        enablePulse = false
                    )
                }
                
                // Step 1: Grant Permissions
                item {
                    HowItWorksCard(
                        step = 1,
                        icon = Icons.Default.Security,
                        title = "Grant Permissions",
                        description = "Allow overlay and screen capture permissions for real-time analysis"
                    )
                }
                
                // Step 2: Open Trading App
                item {
                    HowItWorksCard(
                        step = 2,
                        icon = Icons.Default.ShowChart,
                        title = "Open Trading App",
                        description = "Navigate to your favorite trading platform (TradingView, Robinhood, etc.)"
                    )
                }
                
                // Step 3: Real-Time Detection
                item {
                    HowItWorksCard(
                        step = 3,
                        icon = Icons.Default.Radar,
                        title = "Real-Time Detection",
                        description = "QuantraVision analyzes charts and overlays detected patterns instantly"
                    )
                }
                
                // Section Title: Detection Features
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    NeonText(
                        text = "Detection Features",
                        style = MaterialTheme.typography.headlineMedium,
                        glowColor = NeonGold,
                        enablePulse = false
                    )
                }
                
                // Feature Cards
                item {
                    FeatureCard(
                        icon = Icons.Default.Speed,
                        title = "Sub-Second Latency",
                        description = "Lightning-fast pattern detection with <100ms response time",
                        isPremium = true
                    )
                }
                
                item {
                    FeatureCard(
                        icon = Icons.Default.Vibration,
                        title = "Multi-Modal Alerts",
                        description = "Voice, haptic, and visual notifications for detected patterns",
                        isPremium = false
                    )
                }
                
                item {
                    FeatureCard(
                        icon = Icons.Default.OfflineBolt,
                        title = "100% Offline Processing",
                        description = "All AI computations run locally on your device",
                        isPremium = false
                    )
                }
                
                item {
                    FeatureCard(
                        icon = Icons.Default.TouchApp,
                        title = "Touch-Passthrough Overlay",
                        description = "Interact with trading apps while detection is active",
                        isPremium = true
                    )
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

/**
 * Hero Section with Title and Subtitle
 */
@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NeonText(
            text = "Real-Time Pattern Detection",
            style = MaterialTheme.typography.headlineLarge,
            glowColor = NeonCyan,
            textColor = Color.White,
            enablePulse = true,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            "Advanced AI-powered chart analysis on any trading platform",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

/**
 * Animated Scan Preview Card with NeonBoundingBox
 */
@Composable
private fun ScanPreviewCard() {
    GlassMorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        backgroundColor = Color(0xFF0D1219).copy(alpha = 0.75f),
        borderColor = NeonCyan,
        glowIntensity = 0.8f
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Animated NeonBoundingBox
            NeonBoundingBox(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.75f),
                borderColor = NeonCyan,
                animated = true
            )
            
            // Center Icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlowingIcon(
                    imageVector = Icons.Default.Scanner,
                    contentDescription = null,
                    size = 48.dp,
                    glowColor = NeonCyan,
                    iconColor = Color.White,
                    glowIntensity = 1f
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Detection Preview",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Status Indicator - Ready to Scan with Pulse Animation
 */
@Composable
private fun StatusIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0A0E1A).copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Pulsing status dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = Color(0xFF00FF88).copy(alpha = pulseAlpha),
                    shape = CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            "System Ready • Waiting for Activation",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * How It Works Card with Glass Morphism and Step Number
 */
@Composable
private fun HowItWorksCard(
    step: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    GlassMorphicCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF0D1219).copy(alpha = 0.85f),
        borderColor = NeonCyan.copy(alpha = 0.6f),
        glowIntensity = 0.5f
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step Number with Circular Progress Ring
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularHUDProgress(
                    progress = step / 3f,
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 4.dp,
                    showTicks = false,
                    centerContent = {
                        Text(
                            text = step.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlowingIcon(
                        imageVector = icon,
                        contentDescription = null,
                        size = 20.dp,
                        glowColor = NeonCyan,
                        glowIntensity = 0.7f
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Feature Card with Metallic Design and Premium Highlighting
 */
@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isPremium: Boolean = false
) {
    val borderColor = if (isPremium) NeonGold else NeonCyan.copy(alpha = 0.4f)
    val iconGlow = if (isPremium) NeonGold else NeonCyan
    
    MetallicCard(
        modifier = Modifier.fillMaxWidth(),
        enableShimmer = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isPremium) {
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    NeonGold.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                    } else {
                        Modifier
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with glow
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color(0xFF1A1D23),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                GlowingIcon(
                    imageVector = icon,
                    contentDescription = null,
                    size = 28.dp,
                    glowColor = iconGlow,
                    iconColor = Color.White,
                    glowIntensity = if (isPremium) 1f else 0.7f
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    if (isPremium) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Premium badge
                        Surface(
                            color = NeonGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                "PREMIUM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
