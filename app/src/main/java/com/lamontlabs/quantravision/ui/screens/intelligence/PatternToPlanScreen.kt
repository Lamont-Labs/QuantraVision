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
import com.lamontlabs.quantravision.ui.components.*
import com.lamontlabs.quantravision.ui.theme.*
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground

@Composable
fun PatternToPlanScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    FeatureGate(
        feature = Feature.PATTERN_TO_PLAN,
        onUpgradeClick = onNavigateToPaywall
    ) {
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
                        text = "Trade Scenario Generator",
                        style = AppTypography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    Text(
                        text = "Automatically generates actionable trade scenarios with entry, stop-loss, and target levels based on detected patterns.",
                        style = AppTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    MetallicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(AppSpacing.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bull Flag",
                                    style = AppTypography.titleLarge,
                                    color = AppColors.NeonCyan
                                )
                                TierBadge(tier = SubscriptionTier.PRO)
                            }
                            
                            Spacer(modifier = Modifier.height(AppSpacing.md))
                            
                            TradeScenarioRow("Entry", "$155.20", AppColors.NeonCyan)
                            Divider(color = Color.White.copy(alpha = 0.2f))
                            TradeScenarioRow("Stop Loss", "$152.80", AppColors.Error)
                            Divider(color = Color.White.copy(alpha = 0.2f))
                            TradeScenarioRow("Target", "$161.50", AppColors.Success)
                            Divider(color = Color.White.copy(alpha = 0.2f))
                            TradeScenarioRow("Risk/Reward", "1:2.6", AppColors.NeonGold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    SectionHeader(title = "Recent Scenarios")
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    listOf(
                        "Head & Shoulders" to "Bearish",
                        "Cup & Handle" to "Bullish",
                        "Double Bottom" to "Bullish"
                    ).forEach { (pattern, bias) ->
                        ScenarioCard(pattern, bias)
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeScenarioRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTypography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = AppTypography.titleMedium,
            color = color
        )
    }
}

@Composable
private fun ScenarioCard(pattern: String, bias: String) {
    MetallicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = pattern,
                style = AppTypography.titleSmall,
                color = Color.White
            )
            Text(
                text = bias,
                style = AppTypography.bodyMedium,
                color = if (bias == "Bullish") AppColors.Success else AppColors.Error
            )
        }
    }
}
