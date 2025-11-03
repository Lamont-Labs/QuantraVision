package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.regime.RegimeNavigator
import com.lamontlabs.quantravision.ui.components.AdvancedFeaturesDisclaimerCard
import kotlinx.coroutines.launch

/**
 * RegimeNavigatorScreen
 * 
 * Displays market regime analysis with educational context
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegimeNavigatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var regimeResult by remember { mutableStateOf<RegimeNavigator.MarketRegime?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Regime Navigator") },
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
            // MANDATORY LEGAL DISCLAIMER
            item {
                AdvancedFeaturesDisclaimerCard(collapsible = true)
            }
            
            item {
                Column {
                    Text(
                        "📊 Market Regime Analysis",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Analyze market conditions to understand when patterns historically have higher success rates. Requires recent price data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Demo analysis button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Run Demo Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "This will analyze sample price data to demonstrate regime classification.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isAnalyzing = true
                                    error = null
                                    try {
                                        // Generate sample price data (uptrend with normal volatility)
                                        val samplePrices = generateSamplePriceData()
                                        val regime = RegimeNavigator.analyzeRegime(context, samplePrices)
                                        regimeResult = regime
                                    } catch (e: Exception) {
                                        error = e.message ?: "Analysis failed"
                                    } finally {
                                        isAnalyzing = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isAnalyzing
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Analyzing...")
                            } else {
                                Icon(Icons.Default.Analytics, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Analyze Sample Data")
                            }
                        }
                    }
                }
            }
            
            // Error display
            if (error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Results display
            regimeResult?.let { regime ->
                item {
                    RegimeResultCard(regime)
                }
            }
            
            // Educational info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "ℹ️ How It Works",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Regime Navigator analyzes three key market dimensions:\n\n" +
                            "• Volatility: Measures price fluctuation intensity\n" +
                            "• Trend Strength: Identifies directional clarity\n" +
                            "• Liquidity Proxy: Estimates price action consistency\n\n" +
                            "These combine to classify the overall market regime quality, helping you understand when patterns historically have higher/lower success rates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Disclaimer
            item {
                DisclaimerCard()
            }
        }
    }
}

@Composable
private fun RegimeResultCard(regime: RegimeNavigator.MarketRegime) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (regime.overallQuality) {
                RegimeNavigator.RegimeQuality.FAVORABLE -> MaterialTheme.colorScheme.tertiaryContainer
                RegimeNavigator.RegimeQuality.POOR -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Market Regime",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when (regime.overallQuality) {
                        RegimeNavigator.RegimeQuality.FAVORABLE -> "🟢 Favorable"
                        RegimeNavigator.RegimeQuality.NEUTRAL -> "🟡 Neutral"
                        RegimeNavigator.RegimeQuality.POOR -> "🔴 Poor"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider(Modifier.padding(vertical = 12.dp))
            
            RegimeMetricRow("Volatility", regime.volatility.name)
            RegimeMetricRow("Trend Strength", regime.trendStrength.name)
            RegimeMetricRow("Liquidity", regime.liquidity.name)
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Educational Context:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                regime.educationalContext,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RegimeMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun DisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "⚠️ EDUCATIONAL TOOL ONLY\n\n" +
                "Regime classifications are NOT market predictions, forecasts, or trading recommendations. This is educational context based on historical patterns. Past performance does NOT predict future results. YOU are responsible for all trading decisions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

private fun generateSamplePriceData(): List<Double> {
    // Generate 50 data points with uptrend and normal volatility
    val basePrice = 100.0
    val trend = 0.005 // 0.5% uptrend per period
    val volatility = 0.02 // 2% volatility
    
    return (0 until 50).map { i ->
        val trendComponent = basePrice * (1 + trend * i)
        val randomComponent = (Math.random() - 0.5) * volatility * basePrice
        trendComponent + randomComponent
    }
}
