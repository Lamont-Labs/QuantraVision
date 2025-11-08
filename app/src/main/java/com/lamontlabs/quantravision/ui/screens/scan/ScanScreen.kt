package com.lamontlabs.quantravision.ui.screens.scan

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.*

/**
 * Scan Screen - Clean Pattern Detection Interface
 * Streamlined design matching home screen style
 */
@Composable
fun ScanScreen(
    context: Context,
    onStartScan: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Static brand background
        StaticBrandBackground(modifier = Modifier.fillMaxSize())
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // START DETECTION BUTTON
            item {
                MetallicButton(
                    onClick = onStartScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "START DETECTION",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            // HOW IT WORKS SECTION
            item {
                NeonText(
                    text = "HOW IT WORKS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Steps
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Step 1
                    GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeonCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "1",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Grant Permissions",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Allow overlay & screen capture",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = NeonCyan.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // Step 2
                    GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeonCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "2",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Open Trading App",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Navigate to your charts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = NeonCyan.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // Step 3
                    GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeonCyan.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "3",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Real-Time Detection",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Patterns overlaid instantly",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                Icons.Default.Radar,
                                contentDescription = null,
                                tint = NeonCyan.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            // FEATURES SECTION
            item {
                NeonText(
                    text = "DETECTION FEATURES",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonGold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Sub-Second Latency",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Vibration,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Multi-Modal Alerts",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.OfflineBolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "100% Offline Processing",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = NeonGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Touch-Passthrough Overlay",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                        color = NeonGold.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "💎 PRO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // INFO CARD
            item {
                GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Detection works on any trading platform - TradingView, Robinhood, Webull, and more",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
