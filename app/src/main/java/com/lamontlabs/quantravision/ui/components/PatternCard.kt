package com.lamontlabs.quantravision.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.regime.RegimeNavigator
import com.lamontlabs.quantravision.proof.ProofCapsuleGenerator
import com.lamontlabs.quantravision.planner.PatternToPlanEngine
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatternCard(
    match: PatternMatch,
    onClick: (() -> Unit)? = null,
    showIntelligence: Boolean = true,
    onShowPaywall: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(match.timestamp))
    
    val regimeContext = remember { mutableStateOf<RegimeNavigator.MarketRegime?>(null) }
    var showPlanDialog by remember { mutableStateOf(false) }
    val generatedPlan = remember { mutableStateOf<PatternToPlanEngine.TradeScenario?>(null) }
    var showProofExported by remember { mutableStateOf(false) }
    
    val isPro = remember { ProFeatureGate.isActive(context) }
    
    LaunchedEffect(match, showIntelligence) {
        if (showIntelligence && isPro) {
            try {
                // Regime analysis requires price data - skip for now
                regimeContext.value = null
            } catch (e: Exception) {
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        match.patternName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Confidence: ${(match.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Time: $time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (showIntelligence && isPro && regimeContext.value != null) {
                    RegimeBadge(regimeContext.value!!)
                }
            }
            
            if (showIntelligence) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isPro) {
                                scope.launch {
                                    try {
                                        val engine = PatternToPlanEngine(context)
                                        // generateScenario requires current price - skip for now
                                        // generatedPlan.value = engine.generateScenario(match, currentPrice = 0.0)
                                        // showPlanDialog = true
                                    } catch (e: Exception) {
                                    }
                                }
                            } else {
                                onShowPaywall?.invoke()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isPro) "Plan" else "Plan (Pro)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    OutlinedButton(
                        onClick = {
                            if (isPro) {
                                scope.launch {
                                    try {
                                        val generator = ProofCapsuleGenerator(context)
                                        generator.generateCapsule(match, screenshot = null, regimeContext = regimeContext.value)
                                        showProofExported = true
                                    } catch (e: Exception) {
                                    }
                                }
                            } else {
                                onShowPaywall?.invoke()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isPro) "Proof" else "Proof (Pro)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    if (showPlanDialog && generatedPlan.value != null) {
        TradePlanDialog(
            plan = generatedPlan.value!!,
            onDismiss = { showPlanDialog = false }
        )
    }
    
    if (showProofExported) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showProofExported = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("✅ Proof capsule exported to app storage")
        }
    }
}

@Composable
private fun RegimeBadge(regime: RegimeNavigator.MarketRegime) {
    val (color, emoji) = when (regime.overallQuality) {
        RegimeNavigator.RegimeQuality.FAVORABLE -> MaterialTheme.colorScheme.primary to "🟢"
        RegimeNavigator.RegimeQuality.NEUTRAL -> MaterialTheme.colorScheme.tertiary to "🟡"
        else -> MaterialTheme.colorScheme.error to "🔴"
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(4.dp))
            Text(
                regime.regimeType.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun TradePlanDialog(
    plan: PatternToPlanEngine.TradeScenario,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📊 Trade Scenario") },
        text = {
            Column {
                Text(
                    "⚠️ EDUCATIONAL EXAMPLE ONLY",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Text("Pattern: ${plan.patternMatch.patternName}", style = MaterialTheme.typography.bodyMedium)
                Text("Entry: $${plan.entryPrice}", style = MaterialTheme.typography.bodyMedium)
                Text("Stop Loss: $${plan.stopLoss}", style = MaterialTheme.typography.bodyMedium)
                Text("Take Profit: $${plan.takeProfit}", style = MaterialTheme.typography.bodyMedium)
                Text("Risk/Reward: ${plan.riskRewardRatio}:1", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    plan.educationalContext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
