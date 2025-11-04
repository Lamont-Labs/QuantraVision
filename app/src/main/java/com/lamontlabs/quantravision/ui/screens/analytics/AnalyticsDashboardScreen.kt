package com.lamontlabs.quantravision.ui.screens.analytics

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.analytics.model.WinRateStats
import com.lamontlabs.quantravision.analytics.model.TimeOfDayStats
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import com.lamontlabs.quantravision.ui.success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    context: Context,
    onBack: () -> Unit
) {
    val viewModel = remember { AnalyticsDashboardViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    val isProActive = ProFeatureGate.isActive(context)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Analytics Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = com.lamontlabs.quantravision.ui.SubtleGlowShadow
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (!isProActive) {
            ProUpgradePrompt(modifier = Modifier.padding(padding))
        } else {
            when (val state = uiState) {
                is AnalyticsDashboardViewModel.UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = com.lamontlabs.quantravision.ui.ElectricCyan
                        )
                    }
                }
                is AnalyticsDashboardViewModel.UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = com.lamontlabs.quantravision.ui.NeonRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Failed to load analytics",
                            style = MaterialTheme.typography.titleLarge,
                            color = com.lamontlabs.quantravision.ui.CrispWhite
                        )
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = com.lamontlabs.quantravision.ui.MetallicSilver
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.refreshStats() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.lamontlabs.quantravision.ui.ElectricCyan
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                is AnalyticsDashboardViewModel.UiState.Success -> {
                    AnalyticsContent(
                        stats = state.stats,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    stats: com.lamontlabs.quantravision.analytics.model.OverallPerformanceStats,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            EducationalDisclaimerCard()
        }
        
        item {
            OverallStatsCard(stats)
        }
        
        item {
            Text(
                "Hot Patterns",
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = com.lamontlabs.quantravision.ui.SubtleGlowShadow
                ),
                color = com.lamontlabs.quantravision.ui.ElectricCyan,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(stats.bestPatterns.take(5)) { pattern ->
            PatternPerformanceCard(pattern, isPositive = true)
        }
        
        if (stats.bestPatterns.isEmpty()) {
            item {
                EmptyStateCard("No pattern outcomes tracked yet. Use the feedback feature to track pattern performance.")
            }
        }
        
        item {
            Text(
                "Worst Performing Patterns",
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = com.lamontlabs.quantravision.ui.SubtleGlowShadow
                ),
                color = com.lamontlabs.quantravision.ui.ElectricCyan,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(stats.worstPatterns.take(5)) { pattern ->
            PatternPerformanceCard(pattern, isPositive = false)
        }
        
        if (stats.timeOfDayStats.isNotEmpty()) {
            item {
                Text(
                    "Time Analysis",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        shadow = com.lamontlabs.quantravision.ui.SubtleGlowShadow
                    ),
                    color = com.lamontlabs.quantravision.ui.ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                TimeOfDayHeatmap(stats.timeOfDayStats)
            }
        }
    }
}

@Composable
private fun EducationalDisclaimerCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Educational Statistics Only",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "These statistics are for learning purposes only and do not constitute financial advice. Past performance does not predict future results.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun OverallStatsCard(
    stats: com.lamontlabs.quantravision.analytics.model.OverallPerformanceStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.lamontlabs.quantravision.ui.DarkSurface
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Overall Performance",
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = com.lamontlabs.quantravision.ui.SubtleGlowShadow
                ),
                fontWeight = FontWeight.Bold,
                color = com.lamontlabs.quantravision.ui.ElectricCyan
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Win Rate",
                    value = "${(stats.overallWinRate * 100).toInt()}%",
                    icon = Icons.Default.TrendingUp
                )
                StatItem(
                    label = "Patterns Tracked",
                    value = stats.totalPatterns.toString(),
                    icon = Icons.Default.Analytics
                )
                StatItem(
                    label = "Total Outcomes",
                    value = stats.totalOutcomes.toString(),
                    icon = Icons.Default.Assessment
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(40.dp),
            tint = com.lamontlabs.quantravision.ui.ElectricCyan
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = com.lamontlabs.quantravision.ui.CyanGlowShadow
            ),
            fontWeight = FontWeight.Bold,
            color = com.lamontlabs.quantravision.ui.ElectricCyan
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = com.lamontlabs.quantravision.ui.MetallicSilver
        )
    }
}

@Composable
private fun PatternPerformanceCard(pattern: WinRateStats, isPositive: Boolean) {
    val cardColor = if (isPositive) com.lamontlabs.quantravision.ui.NeonGreen else com.lamontlabs.quantravision.ui.NeonRed
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) 
                com.lamontlabs.quantravision.ui.NeonGreen.copy(alpha = 0.15f)
            else 
                com.lamontlabs.quantravision.ui.NeonRed.copy(alpha = 0.15f)
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
                    pattern.patternName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = com.lamontlabs.quantravision.ui.CrispWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${pattern.totalOutcomes} outcomes: ${pattern.wins}W / ${pattern.losses}L / ${pattern.neutral}N",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.lamontlabs.quantravision.ui.MetallicSilver
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${(pattern.winRate * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = com.lamontlabs.quantravision.ui.SubtleGlowShadow
                    ),
                    fontWeight = FontWeight.Bold,
                    color = cardColor
                )
                Text(
                    "Win Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = com.lamontlabs.quantravision.ui.MetallicSilver
                )
            }
        }
    }
}

@Composable
private fun TimeOfDayHeatmap(stats: List<TimeOfDayStats>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.lamontlabs.quantravision.ui.DarkSurface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Detection Activity by Hour",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = com.lamontlabs.quantravision.ui.ElectricCyan
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            stats.forEach { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stat.timeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.lamontlabs.quantravision.ui.MetallicSilver,
                        modifier = Modifier.width(60.dp)
                    )
                    
                    val maxCount = stats.maxOfOrNull { it.detectionCount } ?: 1
                    val widthFraction = stat.detectionCount.toFloat() / maxCount
                    
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .fillMaxWidth(widthFraction)
                            .background(
                                com.lamontlabs.quantravision.ui.ElectricCyan.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stat.detectionCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = com.lamontlabs.quantravision.ui.MetallicSilver
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.BarChart,
                contentDescription = "Empty",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProUpgradePrompt(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = "Locked",
            modifier = Modifier.size(80.dp),
            tint = com.lamontlabs.quantravision.ui.ElectricCyan
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Analytics Dashboard",
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = com.lamontlabs.quantravision.ui.SubtleGlowShadow
            ),
            color = com.lamontlabs.quantravision.ui.ElectricCyan,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Unlock advanced performance tracking and analytics with Pro",
            style = MaterialTheme.typography.bodyLarge,
            color = com.lamontlabs.quantravision.ui.MetallicSilver
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = com.lamontlabs.quantravision.ui.AmberAccent
            )
        ) {
            Text("Upgrade to Pro")
        }
    }
}
