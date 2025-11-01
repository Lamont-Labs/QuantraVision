package com.lamontlabs.quantravision.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.licensing.AdvancedFeatureGate
import kotlinx.coroutines.launch
import java.io.File

/**
 * AdvancedFeaturesDisclaimerCard
 * 
 * Renders the mandatory legal disclaimer for intelligence stack features
 * Loads content from legal/ADVANCED_FEATURES_DISCLAIMER.md
 */
@Composable
fun AdvancedFeaturesDisclaimerCard(
    modifier: Modifier = Modifier,
    collapsible: Boolean = true,
    requireAcceptance: Boolean = false,
    onAccepted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var disclaimerText by remember { mutableStateOf<String?>(null) }
    var isExpanded by remember { mutableStateOf(!collapsible) }
    var isLoading by remember { mutableStateOf(true) }
    var isAccepted by remember { mutableStateOf(false) }
    var isAccepting by remember { mutableStateOf(false) }
    var acceptanceError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        disclaimerText = AdvancedFeatureGate.getDisclaimerText(context)
        isAccepted = AdvancedFeatureGate.hasAccepted(context)
        isLoading = false
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "⚠️ IMPORTANT LEGAL DISCLAIMER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (collapsible) {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                } else if (disclaimerText != null) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            disclaimerText!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                } else {
                    Text(
                        "EDUCATIONAL TOOLS ONLY - NOT FINANCIAL ADVICE\n\n" +
                        "These features are educational tools for learning trading psychology, technical analysis, " +
                        "and risk management concepts. They do NOT constitute financial advice, investment " +
                        "recommendations, or trading signals. All trading involves substantial risk of loss. " +
                        "Consult a licensed financial advisor before making any investment decisions.\n\n" +
                        "By using these features, you acknowledge:\n" +
                        "• These are educational demonstrations only\n" +
                        "• You will NOT rely on these features as financial advice\n" +
                        "• You accept full responsibility for all trading decisions\n" +
                        "• QuantraVision and Lamont Labs are NOT registered investment advisors\n" +
                        "• Maximum liability is capped at purchase price ($29.99)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (requireAcceptance && !isAccepted) {
                    Spacer(Modifier.height(16.dp))
                    
                    acceptanceError?.let { error ->
                        Text(
                            "❌ $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isAccepting = true
                                    acceptanceError = null
                                    AdvancedFeatureGate.recordAcceptance(context)
                                    isAccepted = true
                                    onAccepted?.invoke()
                                } catch (e: Exception) {
                                    acceptanceError = "Failed to save acceptance: ${e.message}"
                                } finally {
                                    isAccepting = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAccepting
                    ) {
                        if (isAccepting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            if (isAccepting) "Saving..." else "✓ I Accept - Continue to Advanced Features"
                        )
                    }
                }
                
                if (isAccepted) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Disclaimer accepted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Educational tools only • Not financial advice • Click to view full disclaimer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

