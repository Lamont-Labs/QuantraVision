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
                title = { 
                    Text(
                        "Pattern Analytics",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = SubtleGlowShadow
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (hotPatterns.isNotEmpty()) {
                item {
                    Text(
                        "🔥 Hot Patterns This Week",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            shadow = CyanGlowShadow
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(hotPatterns) { hotPattern ->
                    HotPatternCard(hotPattern)
                }

                item { 
                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }

            item {
                Text(
                    "All Pattern Performance",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        shadow = CyanGlowShadow
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (allStats.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No pattern data yet.\nStart detecting patterns to see analytics!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.textSecondary
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
    val trendColor = when (hotPattern.trend) {
        "rising" -> MaterialTheme.colorScheme.success
        "falling" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.metallic
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.amber.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${hotPattern.detectionCount} detections • ${(hotPattern.avgConfidence * 100).toInt()}% avg confidence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textSecondary
                )
            }

            val trendIcon = when (hotPattern.trend) {
                "rising" -> "📈"
                "falling" -> "📉"
                else -> "➡️"
            }

            Surface(
                color = trendColor.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    trendIcon,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

@Composable
fun PatternStatsCard(stats: PatternPerformanceTracker.PatternStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stats.patternName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${stats.totalDetections} total",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn("Avg Confidence", "${(stats.avgConfidence * 100).toInt()}%")
                StatColumn("This Week", "${stats.detectionsThisWeek}")
                StatColumn("This Month", "${stats.detectionsThisMonth}")
            }

            Spacer(Modifier.height(16.dp))

            Column {
                Text(
                    "Confidence Range",
                    style = MaterialTheme.typography.labelLarge.copy(
                        shadow = SubtleGlowShadow
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = stats.avgConfidence.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Low: ${(stats.lowestConfidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textSecondary
                    )
                    Text(
                        "High: ${(stats.highestConfidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Last detected: ${stats.lastDetected}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.textSecondary
            )
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = SubtleGlowShadow
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.textSecondary
        )
    }
}
