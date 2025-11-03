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
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.planner.PatternToPlanEngine
import com.lamontlabs.quantravision.ui.components.AdvancedFeaturesDisclaimerCard
import kotlinx.coroutines.launch

/**
 * PatternPlanScreen
 * 
 * Generate educational trade scenarios from patterns
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternPlanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val planEngine = remember { PatternToPlanEngine(context) }
    
    var scenario by remember { mutableStateOf<PatternToPlanEngine.TradeScenario?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pattern-to-Plan Engine") },
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
                        "📚 Trade Scenario Generator",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Generate educational trade scenarios with theoretical entry/exit prices, stop loss levels, position sizing, and risk/reward analysis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Generate demo scenario button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Generate Demo Scenario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Generate a sample educational trade scenario for a Head & Shoulders pattern.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isGenerating = true
                                    error = null
                                    try {
                                        // Create sample pattern match
                                        val samplePattern = createSamplePattern()
                                        val generatedScenario = planEngine.generateScenario(
                                            patternMatch = samplePattern,
                                            currentPrice = 150.0,
                                            accountSize = 10000.0,
                                            riskPercent = 1.0
                                        )
                                        scenario = generatedScenario
                                    } catch (e: Exception) {
                                        error = e.message ?: "Scenario generation failed"
                                    } finally {
                                        isGenerating = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Generating...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Generate Scenario")
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
            
            // Scenario display
            scenario?.let { tradeScenario ->
                item {
                    TradeScenarioCard(tradeScenario, planEngine)
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
                            "Pattern-to-Plan analyzes detected patterns and generates hypothetical trade scenarios including:\n\n" +
                            "• Theoretical entry price (pattern breakout/breakdown)\n" +
                            "• Stop loss level (pattern invalidation point)\n" +
                            "• Take profit target (measured move calculation)\n" +
                            "• Position sizing example (1% account risk)\n" +
                            "• Risk/Reward ratio analysis\n\n" +
                            "These are EDUCATIONAL EXAMPLES to help you learn trade planning concepts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Disclaimer
            item {
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
                            "⚠️ EDUCATIONAL SCENARIOS ONLY\n\n" +
                            "These are HYPOTHETICAL EXAMPLES for learning. NOT financial advice, investment recommendations, or trading signals. YOU are responsible for all trading decisions. Consult a licensed financial advisor before trading.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeScenarioCard(
    scenario: PatternToPlanEngine.TradeScenario,
    planEngine: PatternToPlanEngine
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "📚 Educational Trade Scenario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Pattern: ${scenario.patternMatch.patternName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Confidence: ${"%.1f".format(scenario.patternMatch.confidence * 100)}%",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Divider(Modifier.padding(vertical = 12.dp))
            
            Text(
                "Theoretical Trade Parameters:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            
            ScenarioRow("Entry Price", "$%.2f".format(scenario.entryPrice))
            ScenarioRow("Stop Loss", "$%.2f".format(scenario.stopLoss))
            ScenarioRow("Take Profit", "$%.2f".format(scenario.takeProfit))
            ScenarioRow("Risk/Reward", "%.2f:1".format(scenario.riskRewardRatio))
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Position Sizing Example (${scenario.accountRiskPercent}% risk):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            
            ScenarioRow("Shares/Units", "%.2f".format(scenario.positionSizeExample))
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "Educational Context:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                scenario.educationalContext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = {
                    planEngine.announceScenario(scenario, includeDisclaimer = true)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Announce Scenario (Voice)")
            }
        }
    }
}

@Composable
private fun ScenarioRow(label: String, value: String) {
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
    Spacer(Modifier.height(4.dp))
}

private fun createSamplePattern(): PatternMatch {
    return PatternMatch(
        id = 0,
        patternName = "Head and Shoulders",
        confidence = 0.85,
        timestamp = System.currentTimeMillis(),
        originPath = "template",
        scale = 1.0,
        timeframe = "1H",
        consensusScore = 0.85,
        detectionBounds = null,
        windowMs = 3600000
    )
}
