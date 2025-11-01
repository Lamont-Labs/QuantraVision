package com.lamontlabs.quantravision.ui

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
import com.lamontlabs.quantravision.psychology.BehavioralGuardrails
import com.lamontlabs.quantravision.ui.components.AdvancedFeaturesDisclaimerCard
import kotlinx.coroutines.launch

/**
 * BehavioralGuardrailsScreen
 * 
 * Display trading psychology stats and warnings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehavioralGuardrailsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val guardrails = remember { BehavioralGuardrails(context) }
    
    var stats by remember { mutableStateOf<BehavioralGuardrails.BehavioralStatistics?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        stats = guardrails.getStatistics()
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Behavioral Guardrails") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, "Reset Statistics")
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
                        "🧠 Trading Psychology Coach",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Monitor your trading behavior to detect emotional patterns like overtrading, revenge trading, and provide discipline reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                stats?.let { statistics ->
                    // Active cooldown warning
                    if (statistics.activeCooldown) {
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
                                        Icons.Default.PauseCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "⏸️ Cooldown Active",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Take a break to avoid emotional trading decisions. Review your trading plan and practice mindfulness.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Stats cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Views",
                                value = "${statistics.viewsLast24h}",
                                subtitle = "Last 24h",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Trades",
                                value = "${statistics.tradesLast7d}",
                                subtitle = "Last 7d",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Win Rate",
                                value = "${"%.1f".format(statistics.winRateLast7d * 100)}%",
                                subtitle = "Last 7d",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Streak",
                                value = if (statistics.currentStreak >= 0) {
                                    "+${statistics.currentStreak}"
                                } else {
                                    "${statistics.currentStreak}"
                                },
                                subtitle = if (statistics.currentStreak >= 0) "Wins" else "Losses",
                                modifier = Modifier.weight(1f),
                                isNegative = statistics.currentStreak < 0
                            )
                        }
                    }
                    
                    // Insights
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "📊 Behavioral Insights",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(12.dp))
                                
                                when {
                                    statistics.viewsLast24h > 20 -> {
                                        InsightRow(
                                            icon = Icons.Default.Warning,
                                            text = "High activity detected (${statistics.viewsLast24h} views). Consider taking breaks to avoid decision fatigue.",
                                            isWarning = true
                                        )
                                    }
                                    statistics.currentStreak < -2 -> {
                                        InsightRow(
                                            icon = Icons.Default.Error,
                                            text = "Negative streak detected. This is a high-risk time for revenge trading. Step back and review your strategy.",
                                            isWarning = true
                                        )
                                    }
                                    statistics.winRateLast7d > 0.7 -> {
                                        InsightRow(
                                            icon = Icons.Default.CheckCircle,
                                            text = "Strong performance! Remember: stay disciplined and avoid overconfidence.",
                                            isWarning = false
                                        )
                                    }
                                    else -> {
                                        InsightRow(
                                            icon = Icons.Default.Info,
                                            text = "Behavior patterns look normal. Continue following your trading plan.",
                                            isWarning = false
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                            "Behavioral Guardrails monitors your app usage to detect patterns that often indicate emotional trading:\n\n" +
                            "• Rapid pattern viewing (overtrading warning)\n" +
                            "• Activity bursts (emotional trading)\n" +
                            "• Loss streaks (revenge trading risk)\n" +
                            "• Cool-down suggestions (break reminders)\n\n" +
                            "All analysis happens on-device. No data is shared or transmitted.",
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
                            "⚠️ EDUCATIONAL REMINDERS ONLY\n\n" +
                            "This is NOT psychological counseling, therapy, or medical advice. These are automated educational reminders. If you have mental health concerns, consult a licensed professional.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
    
    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Behavioral Statistics?") },
            text = { Text("This will clear all behavioral tracking data. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            guardrails.resetState()
                            stats = guardrails.getStatistics()
                            showResetDialog = false
                        }
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isNegative: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isNegative) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isNegative) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InsightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isWarning: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isWarning) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isWarning) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
