package com.lamontlabs.quantravision.ui.screens.intelligence

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.components.*
import com.lamontlabs.quantravision.ui.theme.*
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground

@Composable
fun BehavioralGuardrailsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    FeatureGate(
        feature = Feature.BEHAVIORAL_GUARDRAILS,
        onUpgradeClick = onNavigateToPaywall
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Behavioral Guardrails") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            StaticBrandBackground {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.base)
                ) {
                    NeonText(
                        text = "Trading Psychology Protection",
                        style = AppTypography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    Text(
                        text = "Detects emotional trading patterns and provides real-time warnings to protect you from psychological trading pitfalls.",
                        style = AppTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    SectionHeader(title = "Active Guardrails")
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    listOf(
                        GuardrailData("Revenge Trading", "Monitor rapid scan frequency after losses", true),
                        GuardrailData("FOMO Detection", "Alert on excessive pattern searches", true),
                        GuardrailData("Overconfidence Check", "Warn after consecutive wins", false),
                        GuardrailData("Analysis Paralysis", "Detect excessive re-scanning", true)
                    ).forEach { guardrail ->
                        GuardrailCard(guardrail)
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    SectionHeader(title = "Recent Warnings")
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    MetallicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.md)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AppColors.Warning,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "FOMO Alert",
                                        style = AppTypography.titleSmall,
                                        color = AppColors.Warning
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    Text(
                                        text = "You've scanned 15 times in the last hour. Consider taking a break to avoid emotional decisions.",
                                        style = AppTypography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    Text(
                                        text = "2 hours ago",
                                        style = AppTypography.labelSmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GuardrailData(val name: String, val description: String, val enabled: Boolean)

@Composable
private fun GuardrailCard(guardrail: GuardrailData) {
    MetallicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = guardrail.name,
                    style = AppTypography.titleSmall,
                    color = Color.White
                )
                Text(
                    text = guardrail.description,
                    style = AppTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = guardrail.enabled,
                onCheckedChange = { }
            )
        }
    }
}
