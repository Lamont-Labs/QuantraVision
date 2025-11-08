package com.lamontlabs.quantravision.ui.screens.markets

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * Markets Screen - Intelligence & Pattern Library
 * Clean, streamlined design matching home screen style
 */
@Composable
fun MarketsScreen(
    context: Context,
    onNavigateToTemplates: () -> Unit,
    onNavigateToIntelligence: () -> Unit,
    onNavigateToPredictions: () -> Unit,
    onNavigateToBacktesting: () -> Unit,
    onNavigateToSimilarity: () -> Unit,
    onNavigateToMultiChart: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Static brand background
        StaticBrandBackground(modifier = Modifier.fillMaxSize())
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // PATTERN LIBRARY SECTION
            item {
                NeonText(
                    text = "PATTERN LIBRARY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Template Manager
            item {
                MetallicButton(
                    onClick = onNavigateToTemplates,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "TEMPLATE MANAGER",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Browse all 109 chart patterns",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f)
                    )
                }
            }
            
            // INTELLIGENCE STACK SECTION
            item {
                NeonText(
                    text = "INTELLIGENCE STACK",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonGold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Intelligence Hub - PRO
            item {
                MetallicButton(
                    onClick = onNavigateToIntelligence,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "INTELLIGENCE HUB",
                                fontWeight = FontWeight.Bold
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
                        Text(
                            "Regime Navigator • Pattern-to-Plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Predictions - PRO
            item {
                MetallicButton(
                    onClick = onNavigateToPredictions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "PREDICTIONS",
                                fontWeight = FontWeight.Bold
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
                        Text(
                            "AI-powered pattern forecasting",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Backtesting - PRO
            item {
                MetallicButton(
                    onClick = onNavigateToBacktesting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "BACKTESTING",
                                fontWeight = FontWeight.Bold
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
                        Text(
                            "Test patterns against historical data",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Similarity Search - PRO
            item {
                MetallicButton(
                    onClick = onNavigateToSimilarity,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "SIMILARITY SEARCH",
                                fontWeight = FontWeight.Bold
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
                        Text(
                            "Find similar historical patterns",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Multi-Chart View - PRO
            item {
                MetallicButton(
                    onClick = onNavigateToMultiChart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Dashboard,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "MULTI-CHART VIEW",
                                fontWeight = FontWeight.Bold
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
                        Text(
                            "Analyze multiple timeframes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Info Card
            item {
                GlassMorphicCard(
                    backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)
                ) {
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
                            text = "All pattern recognition runs 100% offline on your device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
