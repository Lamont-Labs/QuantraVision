package com.lamontlabs.quantravision.ui.screens.learning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lamontlabs.quantravision.learning.advanced.*
import com.lamontlabs.quantravision.learning.advanced.model.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedLearningDashboardScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: AdvancedLearningViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Risk", "Behavioral", "Strategy", "Forecasts", "Anomalies")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Learning Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.generateReport(context)
                        }
                    }) {
                        Icon(Icons.Default.Download, "Generate Report")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> OverviewTab(viewModel)
                1 -> RiskTab(viewModel)
                2 -> BehavioralTab(viewModel)
                3 -> StrategyTab(viewModel)
                4 -> ForecastsTab(viewModel)
                5 -> AnomaliesTab(viewModel)
            }
            
            EducationalDisclaimer()
        }
    }
}

@Composable
fun OverviewTab(viewModel: AdvancedLearningViewModel) {
    val portfolioStats by viewModel.portfolioStats.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Portfolio Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    portfolioStats?.let { stats ->
                        MetricRow("Total Patterns", "${stats.totalPatterns}")
                        MetricRow("Average Win Rate", "${(stats.avgWinRate * 100).toInt()}%")
                        MetricRow("Sharpe Ratio", String.format("%.2f", stats.sharpeRatio))
                        MetricRow("Diversification", "${(stats.diversificationScore * 100).toInt()}%")
                    } ?: Text("Loading...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun RiskTab(viewModel: AdvancedLearningViewModel) {
    val bestRiskAdjusted by viewModel.bestRiskAdjusted.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Risk-Adjusted Performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Patterns ranked by Sharpe ratio", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        items(bestRiskAdjusted) { pattern ->
            RiskPatternCard(pattern)
        }
    }
}

@Composable
fun RiskPatternCard(pattern: RankedPattern) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(pattern.patternType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            MetricRow("Win Rate", "${(pattern.winRate * 100).toInt()}%")
            MetricRow("Sharpe Ratio", String.format("%.2f", pattern.sharpeRatio))
            MetricRow("Expected Value", String.format("%.2f", pattern.expectedValue))
            MetricRow("Sample Size", "${pattern.sampleSize} trades")
        }
    }
}

@Composable
fun BehavioralTab(viewModel: AdvancedLearningViewModel) {
    val warnings by viewModel.behavioralWarnings.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Behavioral Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Patterns in your trading behavior", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (warnings.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, "No warnings", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No concerning patterns detected", style = MaterialTheme.typography.bodyLarge)
                        Text("Keep up the disciplined approach!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(warnings) { warning ->
                BehavioralWarningCard(warning)
            }
        }
    }
}

@Composable
fun BehavioralWarningCard(warning: BehavioralWarning) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (warning.severity) {
                WarningSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                WarningSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                WarningSeverity.INFO -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (warning.severity) {
                        WarningSeverity.CRITICAL -> Icons.Default.Error
                        WarningSeverity.WARNING -> Icons.Default.Warning
                        WarningSeverity.INFO -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(warning.type.name.replace("_", " "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(warning.message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Recommendation: ${warning.recommendation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StrategyTab(viewModel: AdvancedLearningViewModel) {
    val portfolio by viewModel.bestPortfolio.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Strategy Recommendations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Optimal pattern portfolio", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        portfolio?.let { pf ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Recommended Portfolio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        MetricRow("Combined Win Rate", "${(pf.combinedWinRate * 100).toInt()}%")
                        MetricRow("Diversification", "${(pf.diversification * 100).toInt()}%")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pattern Allocation:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        pf.allocation.forEach { (pattern, allocation) ->
                            MetricRow(pattern, "${(allocation * 100).toInt()}%")
                        }
                    }
                }
            }
        } ?: item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text("Insufficient data for portfolio recommendations", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun ForecastsTab(viewModel: AdvancedLearningViewModel) {
    val warnings by viewModel.trendWarnings.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Trend Forecasts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Pattern performance trends", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (warnings.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("No significant trends detected", modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(warnings) { warning ->
                TrendWarningCard(warning)
            }
        }
    }
}

@Composable
fun TrendWarningCard(warning: TrendWarning) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (warning.currentTrend) {
                        TrendDirection.IMPROVING -> Icons.Default.TrendingUp
                        TrendDirection.DECLINING -> Icons.Default.TrendingDown
                        TrendDirection.STABLE -> Icons.Default.TrendingFlat
                    },
                    contentDescription = null,
                    tint = when (warning.currentTrend) {
                        TrendDirection.IMPROVING -> MaterialTheme.colorScheme.primary
                        TrendDirection.DECLINING -> MaterialTheme.colorScheme.error
                        TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(warning.patternType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(warning.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AnomaliesTab(viewModel: AdvancedLearningViewModel) {
    val anomalies by viewModel.anomalies.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Anomalies & Alerts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Unusual patterns requiring attention", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        if (anomalies.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("No anomalies detected", modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(anomalies) { anomaly ->
                AnomalyCard(anomaly)
            }
        }
    }
}

@Composable
fun AnomalyCard(anomaly: Anomaly) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (anomaly.type) {
                        AnomalyType.SUDDEN_DROP -> Icons.Default.TrendingDown
                        AnomalyType.SUDDEN_IMPROVEMENT -> Icons.Default.TrendingUp
                        AnomalyType.UNUSUAL_STREAK -> Icons.Default.Stars
                        AnomalyType.PERFORMANCE_SHIFT -> Icons.Default.ChangeCircle
                        AnomalyType.OUTLIER_DETECTION -> Icons.Default.Warning
                    },
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(anomaly.patternType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(anomaly.description, style = MaterialTheme.typography.bodyMedium)
            Text("Z-score: ${String.format("%.2f", anomaly.zScore)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EducationalDisclaimer() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, "Info", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "⚠️ Educational tool only - Not financial advice. Past performance does not predict future results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
