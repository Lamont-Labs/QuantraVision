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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.NeonText

/**
 * Markets Screen - Intelligence & Pattern Library
 * Professional card-based layout with:
 * - Pattern templates library
 * - Intelligence Hub access
 * - Predictions & Analytics
 * - Backtesting tools
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Markets & Intelligence",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            // Pattern Templates Section
            item {
                Text(
                    "Pattern Library",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                FeatureCard(
                    icon = Icons.Default.Category,
                    title = "Template Manager",
                    description = "Browse and configure all 109 chart patterns",
                    badge = "109 Patterns",
                    onClick = onNavigateToTemplates
                )
            }
            
            // Intelligence Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Intelligence Stack",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                FeatureCard(
                    icon = Icons.Default.Psychology,
                    title = "Intelligence Hub",
                    description = "Regime Navigator • Pattern-to-Plan • Behavioral Guardrails",
                    badge = "💎 PRO",
                    onClick = onNavigateToIntelligence
                )
            }
            
            item {
                FeatureCard(
                    icon = Icons.Default.TrendingUp,
                    title = "Predictions",
                    description = "AI-powered pattern outcome forecasting",
                    badge = "💎 PRO",
                    onClick = onNavigateToPredictions
                )
            }
            
            item {
                FeatureCard(
                    icon = Icons.Default.Timeline,
                    title = "Backtesting",
                    description = "Test patterns against historical data",
                    badge = "💎 PRO",
                    onClick = onNavigateToBacktesting
                )
            }
            
            item {
                FeatureCard(
                    icon = Icons.Default.Search,
                    title = "Similarity Search",
                    description = "Find similar historical chart patterns",
                    badge = "💎 PRO",
                    onClick = onNavigateToSimilarity
                )
            }
            
            item {
                FeatureCard(
                    icon = Icons.Default.Dashboard,
                    title = "Multi-Chart View",
                    description = "Analyze multiple timeframes simultaneously",
                    badge = "💎 PRO",
                    onClick = onNavigateToMultiChart
                )
            }
            
            // Info card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "All pattern recognition runs 100% offline on your device using OpenCV template matching",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (badge != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
