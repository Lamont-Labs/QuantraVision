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
                title = { 
                    Text(
                        "Multi-Chart Comparison",
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
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
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Select Charts to Compare",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    shadow = CyanGlowShadow
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Choose 2-4 symbols for correlation analysis",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.textSecondary
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
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Compare Charts (${selectedSymbols.size})")
                            }
                        }
                    }
                }
                
                if (isLoading) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Analyzing correlations...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.textSecondary
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
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    shadow = CyanGlowShadow
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        item {
                            ComparisonSummaryCard(result)
                        }
                        
                        if (result.correlations.isNotEmpty()) {
                            item {
                                Text(
                                    "Cross-Chart Correlations",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        shadow = SubtleGlowShadow
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
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
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        shadow = SubtleGlowShadow
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            )
                        ) {
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
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "No comparison yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Select 2-4 charts above to start correlation analysis",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.textSecondary
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Summary",
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = SubtleGlowShadow
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Charts Analyzed", "${result.charts.size}")
                SummaryItem("Correlations Found", "${result.correlations.size}")
            }
            
            Spacer(Modifier.height(16.dp))
            
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
    val correlationColor = when (correlation.correlation) {
        "strong" -> MaterialTheme.colorScheme.success
        "moderate" -> MaterialTheme.colorScheme.amber
        else -> MaterialTheme.colorScheme.metallic
    }
    
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        correlation.pattern.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Found in ${correlation.symbols.size} charts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textSecondary
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = correlationColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        correlation.correlation.uppercase(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = correlationColor
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Symbols: ${correlation.symbols.joinToString(", ")}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.textSecondary
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Avg Confidence: ${String.format("%.2f", correlation.avgConfidence)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DivergenceCard(divergence: MultiChartComparison.Divergence) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Divergence Detected",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        divergence.symbol1,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        divergence.pattern1.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textSecondary
                    )
                }
                
                Icon(
                    Icons.Default.CompareArrows,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        divergence.symbol2,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        divergence.pattern2.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textSecondary
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = divergence.significance.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surface
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Significance: ${String.format("%.0f%%", divergence.significance * 100)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.primary) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.textSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = SubtleGlowShadow
            ),
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
