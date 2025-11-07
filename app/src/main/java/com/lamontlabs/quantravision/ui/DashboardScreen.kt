package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Redesigned DashboardScreen with Hybrid Layout using MetallicAccordion components
 * 
 * Features:
 * - Hero CTA: Start Detection button (always visible)
 * - 3 expandable chrome accordions for organized navigation
 * - Settings IconButton in TopAppBar
 * - Clean, professional design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    context: Context,
    onStartScan: () -> Unit,
    onReview: () -> Unit,
    onTutorials: () -> Unit,
    onSettings: () -> Unit,
    onTemplates: () -> Unit,
    onAchievements: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    onPredictions: () -> Unit = {},
    onBacktesting: () -> Unit = {},
    onSimilarity: () -> Unit = {},
    onMultiChart: () -> Unit = {},
    onClearHighlights: () -> Unit = {},
    onBook: () -> Unit = {},
    onIntelligence: () -> Unit = {}
) {
    // State for accordion expansion (persistent across configuration changes)
    // Start with all accordions collapsed to fit without scrolling
    var detectionExpanded by rememberSaveable { mutableStateOf(false) }
    var intelligenceExpanded by rememberSaveable { mutableStateOf(false) }
    var learnExpanded by rememberSaveable { mutableStateOf(false) }
    
    // Placeholder for achievement count (can be replaced with actual data fetching)
    val achievementCount by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "QuantraVision Dashboard",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Radial glow background for depth
            RadialGlowBackground(
                glowColor = NeonCyan,
                centerAlpha = 0.15f,
                edgeAlpha = 0f
            )
            
            // Particle starfield
            ParticleStarfield(
                particleCount = 40,
                particleColor = NeonCyan.copy(alpha = 0.6f)
            )
            
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero title with neon glow - smaller and centered
                NeonText(
                    text = "QUANTRAVISION",
                    style = MaterialTheme.typography.headlineSmall,
                    glowColor = NeonCyan,
                    glowIntensity = 1f,
                    enablePulse = true
                )
                
                // Hero CTA with circular HUD progress - more compact
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Circular HUD decoration behind button - smaller
                    CircularHUDProgress(
                        progress = 0.75f,
                        size = 140.dp,
                        strokeWidth = 4.dp,
                        showTicks = true,
                        modifier = Modifier.alpha(0.4f)
                    )
                    
                    // Hero button - more compact
                    MetallicButton(
                        onClick = onStartScan,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp),
                        showTopStrip = true
                    ) {
                        GlowingIcon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            size = 24.dp,
                            glowColor = NeonCyan,
                            glowIntensity = 0.9f
                        )
                        Spacer(Modifier.width(10.dp))
                        NeonText(
                            text = "START DETECTION",
                            style = MaterialTheme.typography.titleSmall,
                            glowColor = NeonCyan,
                            glowIntensity = 0.7f,
                            enablePulse = false
                        )
                    }
                }
            
            // Accordion 1: Detection & Review - compact
            GlassMorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan,
                glowIntensity = 0.7f
            ) {
                MetallicAccordion(
                    title = "Detection & Review",
                    expanded = detectionExpanded,
                    onToggle = { detectionExpanded = !detectionExpanded },
                    icon = {
                        GlowingIcon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            size = 20.dp,
                            glowColor = NeonCyan,
                            glowIntensity = 0.7f
                        )
                    }
                ) {
                MetallicButton(
                    onClick = onReview,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Detections", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            }
            
            // Accordion 2: Intelligence & Analytics (with 💎 badge) - compact
            GlassMorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonGold,
                glowIntensity = 0.8f
            ) {
                MetallicAccordion(
                    title = "Intelligence & Analytics",
                    expanded = intelligenceExpanded,
                    onToggle = { intelligenceExpanded = !intelligenceExpanded },
                    icon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GlowingIcon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                size = 20.dp,
                                glowColor = NeonGold,
                                glowIntensity = 0.8f
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("💎", style = MaterialTheme.typography.titleSmall)
                        }
                    }
                ) {
                MetallicButton(
                    onClick = onIntelligence,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Intelligence Hub", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                
                MetallicButton(
                    onClick = onPredictions,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Predictions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            }
            
            // Accordion 3: Learn & Progress (with achievement count badge) - compact
            GlassMorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan,
                glowIntensity = 0.7f
            ) {
                MetallicAccordion(
                    title = "Learn & Progress",
                    expanded = learnExpanded,
                    onToggle = { learnExpanded = !learnExpanded },
                    badge = if (achievementCount > 0) achievementCount else null,
                    icon = {
                        GlowingIcon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            size = 20.dp,
                            glowColor = NeonCyan,
                            glowIntensity = 0.7f
                        )
                    }
                ) {
                MetallicButton(
                    onClick = onTutorials,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tutorials", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                
                MetallicButton(
                    onClick = onBook,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Trading Book", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                
                MetallicButton(
                    onClick = onAchievements,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Achievements", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
            }
        }
    }
}
}
