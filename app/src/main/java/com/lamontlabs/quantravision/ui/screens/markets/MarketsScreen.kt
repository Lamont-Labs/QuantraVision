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
    onNavigateToMultiChart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                MenuItemCard(
                    title = "TEMPLATE MANAGER",
                    subtitle = "Browse all 109 chart patterns",
                    onClick = onNavigateToTemplates,
                    icon = {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
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
                MenuItemCard(
                    title = "INTELLIGENCE HUB",
                    subtitle = "Regime Navigator • Pattern-to-Plan",
                    onClick = onNavigateToIntelligence,
                    icon = {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    badge = "💎 PRO",
                    badgeColor = NeonGold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Predictions - PRO
            item {
                MenuItemCard(
                    title = "PREDICTIONS",
                    subtitle = "AI-powered pattern forecasting",
                    onClick = onNavigateToPredictions,
                    icon = {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    badge = "💎 PRO",
                    badgeColor = NeonGold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Backtesting - PRO
            item {
                MenuItemCard(
                    title = "BACKTESTING",
                    subtitle = "Test patterns against historical data",
                    onClick = onNavigateToBacktesting,
                    icon = {
                        Icon(
                            Icons.Default.Timeline,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    badge = "💎 PRO",
                    badgeColor = NeonGold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Similarity Search - PRO
            item {
                MenuItemCard(
                    title = "SIMILARITY SEARCH",
                    subtitle = "Find similar historical patterns",
                    onClick = onNavigateToSimilarity,
                    icon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    badge = "💎 PRO",
                    badgeColor = NeonGold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Multi-Chart View - PRO
            item {
                MenuItemCard(
                    title = "MULTI-CHART VIEW",
                    subtitle = "Analyze multiple timeframes",
                    onClick = onNavigateToMultiChart,
                    icon = {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    badge = "💎 PRO",
                    badgeColor = NeonGold,
                    modifier = Modifier.fillMaxWidth()
                )
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
