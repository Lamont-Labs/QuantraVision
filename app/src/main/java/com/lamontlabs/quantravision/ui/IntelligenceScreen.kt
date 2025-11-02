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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import com.lamontlabs.quantravision.ui.components.AdvancedFeaturesDisclaimerCard

/**
 * IntelligenceScreen
 * 
 * Hub for the 4 flagship intelligence features (Pro tier only):
 * 1. Regime Navigator - Market condition analysis
 * 2. Pattern-to-Plan Engine - Trade scenarios
 * 3. Behavioral Guardrails - Psychology coaching
 * 4. Proof Capsules - Shareable detection receipts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntelligenceScreen(
    onBack: () -> Unit,
    onRegimeNavigator: () -> Unit,
    onPatternToPlan: () -> Unit,
    onBehavioralGuardrails: () -> Unit,
    onProofCapsules: () -> Unit,
    onUpgrade: () -> Unit
) {
    val context = LocalContext.current
    val hasProAccess = remember { ProFeatureGate.isActive(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intelligence Stack") },
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
            // Header
            item {
                Column {
                    Text(
                        "🧠 Intelligence Features",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (hasProAccess) {
                            "Advanced AI-powered features to enhance your trading education"
                        } else {
                            "Unlock these premium features with Pro tier ($49.99)"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // MANDATORY LEGAL DISCLAIMER - Requires acceptance before feature access
            item {
                AdvancedFeaturesDisclaimerCard(
                    collapsible = true,
                    requireAcceptance = hasProAccess,
                    onAccepted = {
                        // Acceptance recorded - features now accessible
                    }
                )
            }
            
            // Pro upgrade banner if needed
            if (!hasProAccess) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Pro Features Locked",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Upgrade to Pro ($49.99 one-time) to unlock all 4 intelligence features plus full 109-pattern library, voice alerts, and more.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onUpgrade,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Upgrade, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Upgrade to Pro")
                            }
                        }
                    }
                }
            }
            
            // Feature cards
            item {
                IntelligenceFeatureCard(
                    title = "Regime Navigator",
                    icon = Icons.Default.TrendingUp,
                    description = "Analyze market conditions to understand WHEN patterns have higher success rates. Educational context based on volatility, trend strength, and liquidity.",
                    locked = !hasProAccess,
                    onClick = if (hasProAccess) onRegimeNavigator else onUpgrade
                )
            }
            
            item {
                IntelligenceFeatureCard(
                    title = "Pattern-to-Plan Engine",
                    icon = Icons.Default.CalendarMonth,
                    description = "Generate educational trade scenarios with theoretical entry/exit prices, stop loss levels, position sizing examples, and risk/reward ratios.",
                    locked = !hasProAccess,
                    onClick = if (hasProAccess) onPatternToPlan else onUpgrade
                )
            }
            
            item {
                IntelligenceFeatureCard(
                    title = "Behavioral Guardrails",
                    icon = Icons.Default.Psychology,
                    description = "Trading psychology coaching that detects emotional patterns like revenge trading, overtrading, and provides discipline reminders.",
                    locked = !hasProAccess,
                    onClick = if (hasProAccess) onBehavioralGuardrails else onUpgrade
                )
            }
            
            item {
                IntelligenceFeatureCard(
                    title = "Proof Capsules",
                    icon = Icons.Default.Shield,
                    description = "Package detections into tamper-proof, shareable capsules with SHA-256 hashes, QR codes, and complete audit trails for educational record-keeping.",
                    locked = !hasProAccess,
                    onClick = if (hasProAccess) onProofCapsules else onUpgrade
                )
            }
            
            // Legal disclaimer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "⚠️ Educational Tools Only",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "These intelligence features are EDUCATIONAL TOOLS designed to help you learn technical analysis concepts. They are NOT financial advice, trading recommendations, or investment strategies. All scenarios and analyses are hypothetical. YOU are responsible for all trading decisions. Consult a licensed financial advisor before trading.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
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
private fun IntelligenceFeatureCard(
    title: String,
    icon: ImageVector,
    description: String,
    locked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (locked) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (locked) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (locked) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (locked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
