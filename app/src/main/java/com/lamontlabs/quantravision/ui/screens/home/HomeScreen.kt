package com.lamontlabs.quantravision.ui.screens.home

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.R
import com.lamontlabs.quantravision.ui.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Screen - Cinematic Hero Dashboard
 * Premium AI Trading Terminal with maximum visual impact
 * 
 * Design Features:
 * - QuantumGridBackground with animated technical grid
 * - CandlestickParallax for depth and trading context
 * - MetallicHeroBadge with pulsing QV logo
 * - CircularHUDProgress rings for KPI visualization
 * - Radial scan trigger button with metallic styling
 * - SignalTicker for live AI metrics
 * - GlassMorphicCard for recent detections
 * - MetallicButton for quick actions
 */
@Composable
fun HomeScreen(
    context: Context,
    onStartScan: () -> Unit,
    onViewDetections: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = remember { PatternDatabase.getInstance(context) }
    var recentDetections by remember { mutableStateOf<List<PatternMatch>>(emptyList()) }
    var totalDetections by remember { mutableStateOf(0) }
    var weekDetections by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            val all = db.patternDao().getAll()
            totalDetections = all.size
            
            // Calculate this week's detections
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            weekDetections = all.count { it.timestamp >= weekAgo }
            
            recentDetections = all.sortedByDescending { it.timestamp }.take(5)
        }
    }
    
    // Calculate success rate (simplified - using confidence as proxy)
    val successRate = if (totalDetections > 0) {
        recentDetections.take(10).map { it.confidence }.average().toFloat()
    } else {
        0f
    }
    
    // Simple static background matching the branded logo
    Box(modifier = Modifier.fillMaxSize()) {
        // Static brand background - no animations
        StaticBrandBackground(modifier = Modifier.fillMaxSize())
        
        // Main Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // HERO SECTION - Q Logo with Text
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Hero logo blends with solid black background
                    Image(
                        painter = painterResource(id = R.drawable.hero_logo_final),
                        contentDescription = "QuantraVision - AI Trading Overlay",
                        modifier = Modifier.fillMaxWidth(0.85f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            // KPI Rings - Circular HUD Progress
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Detections Ring
                    CircularHUDProgress(
                        progress = (totalDetections.toFloat() / 100f).coerceAtMost(1f),
                        size = 100.dp,
                        strokeWidth = 8.dp,
                        showTicks = true
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = totalDetections.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // This Week Ring
                    CircularHUDProgress(
                        progress = (weekDetections.toFloat() / 50f).coerceAtMost(1f),
                        size = 100.dp,
                        strokeWidth = 8.dp,
                        showTicks = true
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = weekDetections.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyanBright
                            )
                            Text(
                                text = "Week",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // Success Rate Ring
                    CircularHUDProgress(
                        progress = successRate,
                        size = 100.dp,
                        strokeWidth = 8.dp,
                        showTicks = true
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${(successRate * 100).toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = NeonGold
                            )
                            Text(
                                text = "Rate",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            
            // Scan Button - Just the Q with "TAP TO SCAN"
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing animation
                    val infiniteTransition = rememberInfiniteTransition(label = "scanPulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "buttonScale"
                    )
                    
                    // Scan Q logo - blends with solid black background
                    Image(
                        painter = painterResource(id = R.drawable.q_logo_transparent),
                        contentDescription = "Start Scan",
                        modifier = Modifier
                            .size(140.dp)
                            .scale(scale)
                            .clickable(onClick = onStartScan),
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // "TAP TO SCAN" text
                    Text(
                        text = "TAP TO SCAN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        letterSpacing = 2.sp
                    )
                }
            }
            
            // Activity Summary Card
            item {
                GlassMorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onViewDetections),
                    backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (weekDetections > 0) "$weekDetections Detections This Week" else "No Detections Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "View all patterns →",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = NeonCyan.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            // Quick Actions Section
            item {
                NeonText(
                    text = "QUICK ACTIONS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuItemCard(
                        title = "VIEW ALL DETECTIONS",
                        onClick = onViewDetections,
                        icon = {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    MenuItemCard(
                        title = "PERFORMANCE ANALYTICS",
                        onClick = onNavigateToAnalytics,
                        icon = {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    MenuItemCard(
                        title = "SYSTEM CONFIGURATION",
                        onClick = {
                            android.widget.Toast.makeText(
                                context,
                                "Settings is available in the bottom navigation bar",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        icon = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Detection Glass Card - Glassmorphic card for displaying recent detections
 */
@Composable
private fun DetectionGlassCard(detection: PatternMatch) {
    val time = remember(detection.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(detection.timestamp))
    }
    
    GlassMorphicCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF0D1219).copy(alpha = 0.85f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = detection.patternName.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = NeonCyan.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Confidence Badge
            Surface(
                shape = CircleShape,
                color = when {
                    detection.confidence >= 0.8f -> Color(0xFF00FF88)
                    detection.confidence >= 0.6f -> NeonCyan
                    else -> NeonGold
                }.copy(alpha = 0.2f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(detection.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            detection.confidence >= 0.8f -> Color(0xFF00FF88)
                            detection.confidence >= 0.6f -> NeonCyan
                            else -> NeonGold
                        }
                    )
                }
            }
        }
    }
}
