package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.analytics.PatternPerformanceTracker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val allStats = remember { PatternPerformanceTracker.getAllStats(context) }
    val hotPatterns = remember { PatternPerformanceTracker.getHotPatterns(context, 5) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pattern Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hot Patterns Section
            if (hotPatterns.isNotEmpty()) {
                item {
                    Text(
                        "🔥 Hot Patterns This Week",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(hotPatterns) { hotPattern ->
                    HotPatternCard(hotPattern)
                }

                item { Divider(Modifier.padding(vertical = 8.dp)) }
            }

            // All Patterns Section
            item {
                Text(
                    "All Pattern Performance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (allStats.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No pattern data yet.\nStart detecting patterns to see analytics!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(allStats) { stats ->
                    PatternStatsCard(stats)
                }
            }
        }
    }
}

@Composable
fun HotPatternCard(hotPattern: PatternPerformanceTracker.HotPattern) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    hotPattern.patternName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${hotPattern.detectionCount} detections • ${(hotPattern.avgConfidence * 100).toInt()}% avg confidence",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Trend indicator
            val trendIcon = when (hotPattern.trend) {
                "rising" -> "📈"
                "falling" -> "📉"
                else -> "➡️"
            }
            val trendColor = when (hotPattern.trend) {
                "rising" -> Color(0xFF4CAF50)
                "falling" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                color = trendColor.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    trendIcon,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
fun PatternStatsCard(stats: PatternPerformanceTracker.PatternStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stats.patternName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${stats.totalDetections} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn("Avg Confidence", "${(stats.avgConfidence * 100).toInt()}%")
                StatColumn("This Week", "${stats.detectionsThisWeek}")
                StatColumn("This Month", "${stats.detectionsThisMonth}")
            }

            Spacer(Modifier.height(12.dp))

            // Confidence range
            Column {
                Text(
                    "Confidence Range",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = stats.avgConfidence.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Low: ${(stats.lowestConfidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "High: ${(stats.highestConfidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Last detected
            Spacer(Modifier.height(8.dp))
            Text(
                "Last detected: ${stats.lastDetected}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
