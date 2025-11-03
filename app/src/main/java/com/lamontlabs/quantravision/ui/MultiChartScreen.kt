package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.comparison.MultiChartComparison
import com.lamontlabs.quantravision.integration.FeatureIntegration
import com.lamontlabs.quantravision.ui.paywall.ProUpgradePrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.lamontlabs.quantravision.ui.success
import com.lamontlabs.quantravision.ui.warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChartScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isProActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var comparisonResult by remember { mutableStateOf<MultiChartComparison.ComparisonResult?>(null) }
    var selectedSymbols by remember { mutableStateOf(setOf<String>()) }
    var availableSymbols by remember { mutableStateOf(listOf<String>()) }
    
    LaunchedEffect(Unit) {
        isProActive = FeatureIntegration.canAccessProFeature(context)
        
        availableSymbols = listOf(
            "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA",
            "SPY", "QQQ", "BTC/USD", "ETH/USD", "EUR/USD"
        )
    }
    
    fun runComparison() {
        if (selectedSymbols.size < 2) return
        
        scope.launch {
            isLoading = true
            try {
                val chartAnalyses = selectedSymbols.map { symbol ->
                    val patterns = withContext(Dispatchers.IO) {
                        val db = PatternDatabase.getInstance(context)
                        val oneHourAgo = System.currentTimeMillis() - 3600000L
                        db.patternDao().getRecent(oneHourAgo)
                    }
                    
                    MultiChartComparison.ChartAnalysis(
                        symbol = symbol,
                        timeframe = "1H",
                        patterns = patterns,
                        dominantTrend = determineTrend(patterns.size)
                    )
                }
                
                val result = withContext(Dispatchers.IO) {
                    MultiChartComparison.compareCharts(context, chartAnalyses)
                }
                
                comparisonResult = result
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-Chart Comparison") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!isProActive) {
            ProUpgradePrompt(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                featureName = "Multi-Chart Comparison",
                description = "Compare patterns across multiple charts to detect correlations and divergences. Unlock with Pro."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Select Charts to Compare",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Choose 2-4 symbols for correlation analysis",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(12.dp))
                            
                            availableSymbols.chunked(3).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { symbol ->
                                        FilterChip(
                                            selected = symbol in selectedSymbols,
                                            onClick = {
                                                selectedSymbols = if (symbol in selectedSymbols) {
                                                    selectedSymbols - symbol
                                                } else if (selectedSymbols.size < 4) {
                                                    selectedSymbols + symbol
                                                } else {
                                                    selectedSymbols
                                                }
                                            },
                                            label = { Text(symbol) },
                                            leadingIcon = if (symbol in selectedSymbols) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                            } else null
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Button(
                                onClick = { runComparison() },
                                enabled = selectedSymbols.size >= 2 && !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Compare Charts (${selectedSymbols.size})")
                            }
                        }
                    }
                }
                
                if (isLoading) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator()
                                    Text(
                                        "Analyzing correlations...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                comparisonResult?.let { result ->
                    if (!isLoading) {
                        item {
                            Text(
                                "Comparison Results",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        item {
                            ComparisonSummaryCard(result)
                        }
                        
                        if (result.correlations.isNotEmpty()) {
                            item {
                                Text(
                                    "Cross-Chart Correlations",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            items(result.correlations) { correlation ->
                                CorrelationCard(correlation)
                            }
                        }
                        
                        if (result.divergences.isNotEmpty()) {
                            item {
                                Text(
                                    "Detected Divergences",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            items(result.divergences) { divergence ->
                                DivergenceCard(divergence)
                            }
                        }
                    }
                }
                
                if (comparisonResult == null && !isLoading) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CompareArrows,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "No comparison yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Select 2-4 charts above to start correlation analysis",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonSummaryCard(result: MultiChartComparison.ComparisonResult) {
    val trendAlignment = MultiChartComparison.calculateTrendAlignment(result.charts)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Charts Analyzed", "${result.charts.size}")
                SummaryItem("Correlations Found", "${result.correlations.size}")
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Divergences", "${result.divergences.size}")
                SummaryItem(
                    "Trend Alignment",
                    String.format("%.0f%%", trendAlignment * 100),
                    color = if (trendAlignment >= 0.7) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.warning
                )
            }
        }
    }
}

@Composable
fun CorrelationCard(correlation: MultiChartComparison.CrossChartCorrelation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        correlation.pattern.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Found in ${correlation.symbols.size} charts",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (correlation.correlation) {
                        "strong" -> MaterialTheme.colorScheme.success
                        "moderate" -> MaterialTheme.colorScheme.warning
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Text(
                        correlation.correlation.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Symbols: ${correlation.symbols.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Avg Confidence: ${String.format("%.2f", correlation.avgConfidence)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DivergenceCard(divergence: MultiChartComparison.Divergence) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Divergence Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        divergence.symbol1,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        divergence.pattern1.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Icon(
                    Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        divergence.symbol2,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        divergence.pattern2.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = divergence.significance.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                "Significance: ${String.format("%.0f%%", divergence.significance * 100)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onTertiaryContainer) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun determineTrend(patternCount: Int): String {
    return when {
        patternCount > 5 -> "bullish"
        patternCount < 2 -> "bearish"
        else -> "neutral"
    }
}
