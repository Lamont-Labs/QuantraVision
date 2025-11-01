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
    collapsible: Boolean = true
) {
    val context = LocalContext.current
    var disclaimerText by remember { mutableStateOf<String?>(null) }
    var isExpanded by remember { mutableStateOf(!collapsible) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        disclaimerText = loadDisclaimer(context)
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

/**
 * Load disclaimer from legal/ADVANCED_FEATURES_DISCLAIMER.md
 * Falls back to hardcoded text if file not found
 */
private fun loadDisclaimer(context: Context): String {
    return try {
        // Try to load from assets first
        context.assets.open("legal/ADVANCED_FEATURES_DISCLAIMER.md").bufferedReader().use {
            it.readText()
        }
    } catch (e: Exception) {
        // Fallback: Try external files dir
        try {
            val legalDir = File(context.filesDir.parent, "legal")
            val disclaimerFile = File(legalDir, "ADVANCED_FEATURES_DISCLAIMER.md")
            if (disclaimerFile.exists()) {
                disclaimerFile.readText()
            } else {
                getFallbackDisclaimer()
            }
        } catch (e2: Exception) {
            getFallbackDisclaimer()
        }
    }
}

private fun getFallbackDisclaimer(): String {
    return """
# Advanced Features Legal Disclaimer

**Effective Date:** November 1, 2025

## ⚠️ CRITICAL LEGAL NOTICE

By using QuantraVision's Intelligence Stack features (Regime Navigator, Pattern-to-Plan Engine, Behavioral Guardrails, Proof Capsules), you acknowledge and agree to the following:

## 1. Educational Tools Only

These features are EDUCATIONAL TOOLS designed to help you learn technical analysis, trading psychology, and risk management concepts.

**These features are NOT:**
- Financial advice or investment recommendations
- Trading signals or actionable suggestions
- Market predictions or forecasts
- Personalized investment strategies
- Professional financial planning services

## 2. No Trading Recommendations

All scenarios, calculations, warnings, and classifications are HYPOTHETICAL EXAMPLES for educational purposes only.

## 3. You Are Responsible

**YOU** are solely responsible for:
- All trading decisions
- Conducting your own research
- Managing your own risk and capital
- Understanding that trading involves substantial risk of loss
- Consulting licensed financial advisors before trading

## 4. Risk Disclosure

TRADING INVOLVES SUBSTANTIAL RISK OF LOSS. You may lose your entire investment. No pattern, regime, or strategy guarantees profitability.

## 5. Not Investment Advice

QuantraVision and Lamont Labs are NOT registered investment advisors and do NOT provide investment advice.

## 6. Limitation of Liability

Maximum liability is capped at the purchase price ($29.99 for Pro tier).

## Contact

**Lamont Labs, LLC**  
Email: legal@lamontlabs.io

---

© 2025 Lamont Labs, LLC. All rights reserved.
""".trimIndent()
}
