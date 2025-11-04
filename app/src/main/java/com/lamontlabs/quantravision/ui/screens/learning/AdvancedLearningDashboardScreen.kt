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
import com.lamontlabs.quantravision.ui.CrispWhite
import com.lamontlabs.quantravision.ui.CyanGlowShadow
import com.lamontlabs.quantravision.ui.DarkSurface
import com.lamontlabs.quantravision.ui.DeepNavyBackground
import com.lamontlabs.quantravision.ui.ElectricCyan
import com.lamontlabs.quantravision.ui.MetallicSilver
import com.lamontlabs.quantravision.ui.NeonRed
import com.lamontlabs.quantravision.ui.SubtleGlowShadow
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
    val tabs = listOf("Overview", "Risk", "Behavioral", "Strategy", "Forecasts", "Anomalies", "Scan Insights")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Advanced Learning Analytics",
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = CyanGlowShadow
                        )
                    ) 
                },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = ElectricCyan
                )
            )
        },
        containerColor = DeepNavyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = ElectricCyan
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        selectedContentColor = ElectricCyan,
                        unselectedContentColor = MetallicSilver
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
                6 -> ScanInsightsTab()
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
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Portfolio Overview", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    portfolioStats?.let { stats ->
                        MetricRow("Total Patterns", "${stats.totalPatterns}")
                        MetricRow("Average Win Rate", "${(stats.avgWinRate * 100).toInt()}%")
                        MetricRow("Sharpe Ratio", String.format("%.2f", stats.sharpeRatio))
                        MetricRow("Diversification", "${(stats.diversificationScore * 100).toInt()}%")
                    } ?: Text("Loading...", style = MaterialTheme.typography.bodyMedium, color = MetallicSilver)
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
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Risk-Adjusted Performance", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Text(
                "Patterns ranked by Sharpe ratio", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MetallicSilver
            )
        }
        
        items(bestRiskAdjusted) { pattern ->
            RiskPatternCard(pattern)
        }
    }
}

@Composable
fun RiskPatternCard(pattern: RankedPattern) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                pattern.patternType, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Spacer(modifier = Modifier.height(12.dp))
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
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Behavioral Insights", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Text(
                "Patterns in your trading behavior", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MetallicSilver
            )
        }
        
        if (warnings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle, 
                            "No warnings", 
                            modifier = Modifier.size(48.dp), 
                            tint = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No concerning patterns detected", 
                            style = MaterialTheme.typography.bodyLarge,
                            color = CrispWhite
                        )
                        Text(
                            "Keep up the disciplined approach!", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MetallicSilver
                        )
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
                WarningSeverity.CRITICAL -> DarkSurface
                WarningSeverity.WARNING -> DarkSurface
                WarningSeverity.INFO -> DarkSurface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (warning.severity) {
                        WarningSeverity.CRITICAL -> Icons.Filled.Warning
                        WarningSeverity.WARNING -> Icons.Default.Warning
                        WarningSeverity.INFO -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when (warning.severity) {
                        WarningSeverity.CRITICAL -> NeonRed
                        WarningSeverity.WARNING -> ElectricCyan
                        WarningSeverity.INFO -> MetallicSilver
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    warning.type.name.replace("_", " "), 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = CrispWhite
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                warning.message, 
                style = MaterialTheme.typography.bodyMedium,
                color = CrispWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Recommendation: ${warning.recommendation}", 
                style = MaterialTheme.typography.bodySmall, 
                color = MetallicSilver
            )
        }
    }
}

@Composable
fun StrategyTab(viewModel: AdvancedLearningViewModel) {
    val portfolio by viewModel.bestPortfolio.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Strategy Recommendations", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Text(
                "Optimal pattern portfolio", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MetallicSilver
            )
        }
        
        portfolio?.let { pf ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Recommended Portfolio", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MetricRow("Combined Win Rate", "${(pf.combinedWinRate * 100).toInt()}%")
                        MetricRow("Diversification", "${(pf.diversification * 100).toInt()}%")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Pattern Allocation:", 
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.Bold,
                            color = CrispWhite
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        pf.allocation.forEach { (pattern, allocation) ->
                            MetricRow(pattern, "${(allocation * 100).toInt()}%")
                        }
                    }
                }
            }
        } ?: item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    "Insufficient data for portfolio recommendations", 
                    modifier = Modifier.padding(24.dp),
                    color = MetallicSilver
                )
            }
        }
    }
}

@Composable
fun ForecastsTab(viewModel: AdvancedLearningViewModel) {
    val warnings by viewModel.trendWarnings.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Trend Forecasts", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Text(
                "Pattern performance trends", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MetallicSilver
            )
        }
        
        if (warnings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        "No significant trends detected", 
                        modifier = Modifier.padding(24.dp),
                        color = MetallicSilver
                    )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (warning.currentTrend) {
                        TrendDirection.IMPROVING -> Icons.Default.TrendingUp
                        TrendDirection.DECLINING -> Icons.Default.TrendingDown
                        TrendDirection.STABLE -> Icons.Default.TrendingFlat
                    },
                    contentDescription = null,
                    tint = when (warning.currentTrend) {
                        TrendDirection.IMPROVING -> ElectricCyan
                        TrendDirection.DECLINING -> NeonRed
                        TrendDirection.STABLE -> MetallicSilver
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    warning.patternType, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = CrispWhite
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                warning.message, 
                style = MaterialTheme.typography.bodyMedium,
                color = CrispWhite
            )
        }
    }
}

@Composable
fun AnomaliesTab(viewModel: AdvancedLearningViewModel) {
    val anomalies by viewModel.anomalies.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Anomalies & Alerts", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Text(
                "Unusual patterns requiring attention", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MetallicSilver
            )
        }
        
        if (anomalies.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        "No anomalies detected", 
                        modifier = Modifier.padding(24.dp),
                        color = MetallicSilver
                    )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (anomaly.type) {
                        AnomalyType.SUDDEN_DROP -> Icons.Default.TrendingDown
                        AnomalyType.SUDDEN_IMPROVEMENT -> Icons.Default.TrendingUp
                        AnomalyType.UNUSUAL_STREAK -> Icons.Default.Stars
                        AnomalyType.PERFORMANCE_SHIFT -> Icons.Default.ChangeCircle
                        AnomalyType.OUTLIER_DETECTION -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = when (anomaly.type) {
                        AnomalyType.SUDDEN_DROP -> NeonRed
                        AnomalyType.SUDDEN_IMPROVEMENT -> ElectricCyan
                        else -> MetallicSilver
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    anomaly.patternType, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = CrispWhite
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                anomaly.description, 
                style = MaterialTheme.typography.bodyMedium,
                color = CrispWhite
            )
            Text(
                "Z-score: ${String.format("%.2f", anomaly.zScore)}", 
                style = MaterialTheme.typography.bodySmall, 
                color = MetallicSilver
            )
        }
    }
}

@Composable
fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MetallicSilver
        )
        Text(
            value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Bold,
            color = ElectricCyan
        )
    }
}

@Composable
fun ScanInsightsTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scanStats by remember { mutableStateOf<com.lamontlabs.quantravision.learning.ScanStatistics?>(null) }
    var frequentPatterns by remember { mutableStateOf<List<com.lamontlabs.quantravision.learning.PatternFrequencyInfo>>(emptyList()) }
    var topCooccurrences by remember { mutableStateOf<List<com.lamontlabs.quantravision.learning.CooccurrenceInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val engine = com.lamontlabs.quantravision.learning.ScanLearningEngine(context)
                scanStats = engine.getScanStats()
                frequentPatterns = engine.getMostFrequentPatterns(10)
                topCooccurrences = engine.getPatternCooccurrences()
            } catch (e: Exception) {
                timber.log.Timber.e(e, "Failed to load scan insights")
            } finally {
                isLoading = false
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                "Scan Learning Insights", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Text(
                "Intelligence from every chart scan", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MetallicSilver
            )
        }
        
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ElectricCyan)
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Scan Statistics", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        scanStats?.let { stats ->
                            MetricRow("Scans This Week", "${stats.totalScansWeek}")
                            MetricRow("Scans This Month", "${stats.totalScansMonth}")
                            MetricRow("Unique Patterns", "${stats.uniquePatternsDetected}")
                            MetricRow("Most Common", stats.mostCommonPattern)
                        }
                    }
                }
            }
            
            item {
                Text(
                    "Most Frequent Patterns", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Text(
                    "Patterns detected most often in your scans", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MetallicSilver
                )
            }
            
            if (frequentPatterns.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            "No scan data yet. Start scanning charts to see insights!", 
                            modifier = Modifier.padding(24.dp),
                            color = MetallicSilver
                        )
                    }
                }
            } else {
                items(frequentPatterns.size) { index ->
                    val pattern = frequentPatterns[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                pattern.patternName, 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            MetricRow("Total Detections", "${pattern.detectionCount}")
                            MetricRow("Detection Rate", "${(pattern.detectionRate * 100).toInt()}%")
                            MetricRow("Avg Confidence", "${(pattern.avgConfidence * 100).toInt()}%")
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Pattern Co-occurrence", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Text(
                    "Patterns that frequently appear together", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MetallicSilver
                )
            }
            
            if (topCooccurrences.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            "Not enough data for co-occurrence analysis", 
                            modifier = Modifier.padding(24.dp),
                            color = MetallicSilver
                        )
                    }
                }
            } else {
                items(topCooccurrences.size) { index ->
                    val cooccurrence = topCooccurrences[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    cooccurrence.pattern1, 
                                    style = MaterialTheme.typography.bodyMedium, 
                                    fontWeight = FontWeight.Bold,
                                    color = CrispWhite
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    Icons.Default.SwapHoriz, 
                                    "with", 
                                    modifier = Modifier.size(16.dp),
                                    tint = ElectricCyan
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    cooccurrence.pattern2, 
                                    style = MaterialTheme.typography.bodyMedium, 
                                    fontWeight = FontWeight.Bold,
                                    color = CrispWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            MetricRow("Co-occurrence Count", "${cooccurrence.cooccurrenceCount}")
                            MetricRow("Co-occurrence Rate", "${(cooccurrence.cooccurrenceRate * 100).toInt()}%")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EducationalDisclaimer() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info, 
                "Info", 
                modifier = Modifier.size(20.dp),
                tint = ElectricCyan
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "⚠️ Educational tool only - Not financial advice. Past performance does not predict future results.",
                style = MaterialTheme.typography.bodySmall,
                color = CrispWhite
            )
        }
    }
}
